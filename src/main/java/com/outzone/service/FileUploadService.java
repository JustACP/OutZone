package com.outzone.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.outzone.mapper.*;
import com.outzone.pojo.*;
import com.outzone.util.MergeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@Service
public class FileUploadService{
    @Value("${upload.file.path}")
    private String uploadFilePath;

    @Resource
    FileMapper fileMapper;
    @Resource
    DirectoryMapper directoryMapper;
    @Resource
    UserFileMapper userFileMapper;
    @Resource
    SecurityContextService securityContextService;

    @Resource
    GroupFileMapper groupFileMapper;

    @Resource
    GroupMapper groupMapper;

    public ResponseResult upload(MultipartFileParamsVO fileParams){

        String filePath = uploadFilePath +fileParams.getIdentifier() +"/chunk/" +fileParams.getChunkNumber();
        File fileTemp = new File(filePath);

        File parentFile = fileTemp.getParentFile();

        if(!parentFile.exists()){
            parentFile.mkdirs();
        }
        try{
            MultipartFile file = fileParams.getFile();
            //transerTo 只能使用一次
            file.transferTo(fileTemp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ResponseResult(HttpStatus.OK.value(), "分片上传成功");
    }

    public ResponseResult checkFileAndChunks(MultipartFileParamsVO fileParamsVO, HttpServletResponse response,UserDTO userDTO){
        ResponseResult responseResult = new ResponseResult<>();
        if(checkFile(fileParamsVO,userDTO)){
            responseResult.setCode(HttpStatus.OK.value());
            responseResult.setMsg("文件已存在");
            Map<String,Boolean> isSkip = new HashMap<>();
            isSkip.put("isSkip",true);
            responseResult.setData(isSkip);
            uploadExistUserFile(fileParamsVO, userDTO);
            return responseResult;
        }

        String fileDir = fileParamsVO.getIdentifier();
        String fileName = fileParamsVO.getFilename();

        // 分片目录
        String chunkPath = uploadFilePath  + fileDir+ "/chunk/";

        //分片目录  对象
        File file = new File(chunkPath);
        List<File> chunkFileList = MergeUtil.chunkFileList(file);


        String[] temp;

        if(chunkFileList ==null){
            temp = new String[0];
        }else{
            temp = new String[chunkFileList.size()];



            if(chunkFileList.size() >0){
                for(int i = 0;i<chunkFileList.size();i++){
                    temp[i] = chunkFileList.get(i).getName();//保存已存在文件列表
                }
            }
        }

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uploadedList",temp);

        return new ResponseResult<HashMap>(HttpStatus.OK.value(), "已存在分片数",hashMap);
    }





    public ResponseResult uploadSuccess(UploadFileInfo uploadFileInfo){

        String chunkPath = uploadFilePath+  uploadFileInfo.getIdentifier()+"/chunk/";
        String mergePath = uploadFilePath+ uploadFileInfo.getIdentifier() + "/";
        File file = MergeUtil.mergeFile(uploadFilePath,chunkPath,mergePath, uploadFileInfo.getFilename());
        if(file == null){

            return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "文件合并失败");
        }

        return new ResponseResult<>(HttpStatus.OK.value(), "文件合并成功");

    }

    /**
     * 检查文件是否已经存在
     *
     * @param fileParamsVO
     * @Author re1ife
     **/
    public boolean checkFile(MultipartFileParamsVO fileParamsVO,UserDTO uploadUserDTO){

        String fileMd5 = fileParamsVO.getIdentifier();


        LambdaQueryWrapper<FileDTO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FileDTO::getMd5,fileMd5);
        FileDTO isExist = fileMapper.selectOne(lambdaQueryWrapper);


        if(Objects.isNull(isExist)){

            return false;
        }

//        if(groupId.equals("-1")){
//
//
//            UserFileDTO toInsertUploadInfo = new UserFileDTO();
//
//            directoryWrapper.eq(DirectoryDTO::getName,parentDir)
//                    .eq(DirectoryDTO::isGroupDirectory,false);
//            DirectoryDTO parentDirObj = directoryMapper.selectOne(directoryWrapper);
//
//            toInsertUploadInfo.setFileName(fileName)
//                            .setFileId(isExist.getId())
//                            .setAbsolutePath(uploadCloudPath)
//                            .setParentDirectoryId(parentDirObj.getDirectoryId())
//                            .setUploadDate(new Timestamp(new Date().getTime()))
//                            .setUserId(uploadUserDTO.getId());
//            userFileMapper.insert(toInsertUploadInfo);
//
//        }else{
//            GroupFileDTO toInsertUploadInfo = new GroupFileDTO();
//            LambdaQueryWrapper<GroupsDTO> groupWrapper = new LambdaQueryWrapper<>();
//
//            groupWrapper.eq(GroupsDTO::getGroupId,Long.valueOf(groupId)).eq(GroupsDTO::getUserId, uploadUserDTO.getId());
//            GroupsDTO uploadGroup = groupMapper.selectOne(groupWrapper);
//            directoryWrapper.eq(DirectoryDTO::getName,parentDir)
//                    .eq(DirectoryDTO::isGroupDirectory,true);
//
//            DirectoryDTO parentDirObj = directoryMapper.selectOne(directoryWrapper);
//            toInsertUploadInfo.setFileName(fileName)
//                    .setFileId(isExist.getId())
//                    .setAbsolutePath(uploadCloudPath)
//                    .setParentDirectoryId(parentDirObj.getDirectoryId())
//                    .setUploadDate(new Timestamp(new Date().getTime()))
//                    .setUserId(uploadUserDTO.getId())
//                    .setGroupId(uploadGroup.getGroupId());
//            groupFileMapper.insert(toInsertUploadInfo);
//
//        }
//        isExist.setCount(isExist.getCount()+1);
//        fileMapper.updateById(isExist);
        return true;





    }

    public void uploadExistUserFile(MultipartFileParamsVO fileParamsVO,UserDTO userDTO){

        String fileMd5 = fileParamsVO.getIdentifier();
        String fileName = fileParamsVO.getFilename();
        String uploadCloudPath = fileParamsVO.getUploadCloudPath();


        LambdaQueryWrapper<FileDTO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FileDTO::getMd5,fileMd5);

        FileDTO isExist = fileMapper.selectOne(lambdaQueryWrapper);

        String parentDir = uploadCloudPath.substring(uploadCloudPath.lastIndexOf("/"));
        LambdaQueryWrapper<DirectoryDTO> directoryWrapper = new LambdaQueryWrapper<>();

        UserFileDTO toInsertUploadInfo = new UserFileDTO();

        directoryWrapper.eq(DirectoryDTO::getAbsolutePath,parentDir)
                .eq(DirectoryDTO::isGroupDirectory,false)
                .eq(DirectoryDTO::getOwnerId,userDTO.getId());
        DirectoryDTO parentDirObj = directoryMapper.selectOne(directoryWrapper);

        toInsertUploadInfo.setFileName(fileName)
                .setFileId(isExist.getId())
                .setAbsolutePath(uploadCloudPath)
                .setParentDirectoryId(parentDirObj.getDirectoryId())
                .setUploadDate(new Timestamp(new Date().getTime()))
                .setUserId(userDTO.getId());
        isExist.setCount(isExist.getCount()+1);
        fileMapper.updateById(isExist);
        userFileMapper.insert(toInsertUploadInfo);


    }

    public void uploadExistGroupFile(MultipartFileParamsVO fileParamsVO,UserDTO userDTO){
        String groupId = fileParamsVO.getGroupId();
        String fileMd5 = fileParamsVO.getIdentifier();
        String fileName = fileParamsVO.getFilename();
        String uploadCloudPath = fileParamsVO.getUploadCloudPath();


        LambdaQueryWrapper<FileDTO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FileDTO::getMd5,fileMd5);

        FileDTO isExist = fileMapper.selectOne(lambdaQueryWrapper);

        String parentDir = uploadCloudPath.substring(uploadCloudPath.lastIndexOf("/"));
        LambdaQueryWrapper<DirectoryDTO> directoryWrapper = new LambdaQueryWrapper<>();

        GroupFileDTO toInsertUploadInfo = new GroupFileDTO();

        directoryWrapper.eq(DirectoryDTO::getName,parentDir)
                .eq(DirectoryDTO::isGroupDirectory,true);
        DirectoryDTO parentDirObj = directoryMapper.selectOne(directoryWrapper);

        toInsertUploadInfo.setFileName(fileName)
                .setFileId(isExist.getId())
                .setAbsolutePath(uploadCloudPath)
                .setParentDirectoryId(parentDirObj.getDirectoryId())
                .setUploadDate(new Timestamp(new Date().getTime()))
                .setUserId(userDTO.getId())
                .setGroupId(Long.valueOf(groupId));
        isExist.setCount(isExist.getCount()+1);
        fileMapper.updateById(isExist);
        groupFileMapper.insert(toInsertUploadInfo);


    }





}
