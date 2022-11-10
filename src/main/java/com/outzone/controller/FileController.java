package com.outzone.controller;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.outzone.mapper.*;
import com.outzone.pojo.*;
import com.outzone.service.FileUploadService;
import com.outzone.service.SecurityContextService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/file")
public class FileController {

    private final static String utf8 = "UTF-8";
    @Value("${upload.file.path}")
    private String uploadFilePath;
    @Resource
    private FileUploadService fileUploadService;

    @Resource
    UserFileMapper userFileMapper;
    @Resource
    GroupFileMapper groupFileMapper;
    @Resource
    FileMapper fileMapper;
    @Resource
    SecurityContextService securityContextService;
    @Resource
    DirectoryMapper directoryMapper;
    @Resource
    GroupMapper groupMapper;
    /**
     * 上传前调用(只调一次)，判断文件是否已经被上传完成，如果是，跳过，
     * 如果不是，判断是否传了一半，如果是，将缺失的分片编号返回，让前端传输缺失的分片即可
     */
    @GetMapping("/upload")
    @ResponseBody
    public ResponseResult checkFile (MultipartFileParamsVO fileParamsVO , HttpServletResponse response) throws IOException {
//        UserDTO userDTO = ((LoginUserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserDTO();
        UserDTO userDTO = securityContextService.getUserFromContext().getUserDTO();
        response.setStatus(204);

        return fileUploadService.checkFileAndChunks(fileParamsVO,response,userDTO);


    }


    /**
     * 上传调用
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ResponseBody
    public ResponseResult upload(MultipartFileParamsVO file){

        return fileUploadService.upload(file);
    }

    /**
     * 上传完成调用，进行分片文件合并
     */
    @PostMapping("/merge")
    @ResponseBody
    public ResponseResult uploadSuccess(@RequestBody  UploadFileInfo file){

        ResponseResult res = fileUploadService.uploadSuccess(file);
        if(res.getCode() == HttpStatus.OK.value()){

            FileDTO newFile = new FileDTO();
            newFile

                    .setFileSize(file.getTotalSize())
                    .setFilename(file.getFilename())
                    .setMd5(file.getIdentifier())
                    .setCount(0)
                    .setPhysicalPath(uploadFilePath+file.getIdentifier())
                    .setUploadtime(new Timestamp(new Date().getTime()))
                    .setFileType(file.getFilename().substring(file.getFilename().lastIndexOf(".")));

            fileMapper.insert(newFile);
            DirectoryDTO parentDirectoryDto = new DirectoryDTO();
            LambdaQueryWrapper<DirectoryDTO> directoryWrapper = new LambdaQueryWrapper<>();
            UserDTO nowUser = securityContextService.getUserFromContext().getUserDTO();

            newFile = fileMapper.selectOne(new LambdaQueryWrapper<FileDTO>().eq(FileDTO::getMd5,newFile.getMd5()));

            if(file.getGroupId().equals("-1")){
                fileUploadService.uploadExistUserFile(new MultipartFileParamsVO().setByUploadFileInfo(file),nowUser);
//
//                UserFileDTO userFile = new UserFileDTO();
//
//                directoryWrapper.eq(DirectoryDTO::getAbsolutePath,file.getUploadCloudPath())
//                        .eq(DirectoryDTO::isGroupDirectory,false)
//                        .eq()
//                parentDirectoryDto = directoryMapper.selectOne(directoryWrapper);
//                userFile.setFileId(newFile.getId())
//                        .setUploadDate(new Timestamp(new Date().getTime()))
//                        .setUserId(nowUser.getId())
//                        .setAbsolutePath(file.getUploadCloudPath())
//                        .setParentDirectoryId(parentDirectoryDto.getDirectoryId())
//                        .setFileName(file.getFilename());
//
//                userFileMapper.insert(userFile);
            }else{
                GroupFileDTO groupFiles = new GroupFileDTO();
                directoryWrapper.eq(DirectoryDTO::getAbsolutePath,file.getUploadCloudPath()).eq(DirectoryDTO::isGroupDirectory,true);
                parentDirectoryDto = directoryMapper.selectOne(directoryWrapper);

                groupFiles.setFileId(newFile.getId())
                        .setUploadDate(new Timestamp(new Date().getTime()))
                        .setUserId(nowUser.getId())
                        .setAbsolutePath(file.getUploadCloudPath())
                        .setParentDirectoryId(parentDirectoryDto.getDirectoryId())
                        .setFileName(file.getFilename())
                        .setGroupId(Long.valueOf(file.getGroupId()));

                groupFileMapper.insert(groupFiles);

            }
        }
        return res;
    }



    @PostMapping("/getNowFileList")
    @ResponseBody
    public ResponseResult getNowUserDirectoryFileLlist(@RequestBody String absolutePathJSON){

        UserDTO nowUser = securityContextService.getUserFromContext().getUserDTO();
        String groupId = (String) JSONObject.parseObject(absolutePathJSON).get("groupId");
        String absolutePath = (String) JSONObject.parseObject(absolutePathJSON).get("absolutePath");
        List<ContentVO> directoryList = null;
        List<ContentVO> resList = null;

        if(Objects.isNull(groupId) ||groupId=="-1" ){

            directoryList = directoryMapper.getDirList(absolutePath, nowUser.getId(), false);
            resList = userFileMapper.getUserFileList(absolutePath, nowUser.getId());
            resList.addAll(directoryList);

        }else{
            GroupsDTO isGroupUser = groupMapper.selectOne(new LambdaQueryWrapper<GroupsDTO>()
                    .eq(GroupsDTO::getGroupId,Long.valueOf(groupId))
                    .eq(GroupsDTO::getUserId,nowUser.getId()));
            if(!Objects.isNull(isGroupUser)){
                directoryList = directoryMapper.getDirList(absolutePath, Long.valueOf(groupId),true);
                resList = groupFileMapper.getGroupFileList(absolutePath, Long.valueOf(groupId));
                resList.addAll(directoryList);
            }

        }


        return  new ResponseResult<List>(HttpStatus.OK.value(), "当前目录下文件",resList);
    }

//    @PostMapping("/download")
//    public ResponseResult download(UploadFileInfo fileInfo){
//        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
//
//        if(fileInfo.get)
//    }







}
