package com.outzone.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.outzone.mapper.*;
import com.outzone.pojo.*;
import com.outzone.util.IdGeneratorUtil;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class FileUploadService{
    @Value("${upload.file.path}")
    private String uploadFilePath;
    private String uploadIconPath = uploadFilePath + "icon/";
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

    public ResponseResult uploadIcon(MultipartFileParamsVO fileParams){

        String filePath = uploadIconPath + fileParams.getIdentifier()+".png";
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
        return new ResponseResult(HttpStatus.OK.value(), "缩略图上传成功");
    }

    public ResponseResult checkFileAndChunks(MultipartFileParamsVO fileParamsVO, HttpServletResponse response,UserDTO userDTO){
        ResponseResult responseResult = new ResponseResult<>();
//        if(checkFile(fileParamsVO,userDTO)){
//            responseResult.setCode(HttpStatus.OK.value());
//            responseResult.setMsg("文件已存在");
//            Map<String,Boolean> isSkip = new HashMap<>();
//            isSkip.put("isSkip",true);
//            isSkip.put("needMerge", false);
//            responseResult.setData(isSkip);
//
//            if(fileParamsVO.getGroupId()==-1){
//                uploadExistUserFile(fileParamsVO, userDTO);
//            }else{
//                uploadExistGroupFile(fileParamsVO,userDTO);
//            }
//
//            return responseResult;
//        }

        String fileDir = fileParamsVO.getIdentifier();
        String fileName = fileParamsVO.getFilename();

        // 分片目录
        String chunkPath = uploadFilePath  + fileDir+ "/chunk/"+fileParamsVO.getChunkNumber();

        //分片目录  对象
        File file = new File(chunkPath);
        Map<String,Boolean> hashMap = new HashMap<>();
        if(file.exists()){

            hashMap.put("isSkipChunk",true);
            hashMap.put("needMerge",true);
            responseResult.setCode(200);
            responseResult.setMsg("已存在分块");

        }else{
            hashMap.put("isSkipChunk",false);
            hashMap.put("needMerge",true);

            responseResult.setCode(210);
            responseResult.setMsg("需要上传分片");

        }
//        List<File> chunkFileList = MergeUtil.chunkFileList(file);


//        String[] temp;
//
//        if(chunkFileList ==null){
//            temp = new String[0];
//        }else{
//            temp = new String[chunkFileList.size()];
//
//
//
//            if(chunkFileList.size() >0){
//                for(int i = 0;i<chunkFileList.size();i++){
//                    temp[i] = chunkFileList.get(i).getName();//保存已存在文件列表
//                }
//            }
//        }

        responseResult.setData(hashMap);
        return responseResult;
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
                .setId(IdGeneratorUtil.generateId())
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
        Long groupId = fileParamsVO.getGroupId();
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
                .setId(IdGeneratorUtil.generateId())
                .setFileId(isExist.getId())
                .setAbsolutePath(uploadCloudPath)
                .setParentDirectoryId(parentDirObj.getDirectoryId())
                .setUploadDate(new Timestamp(new Date().getTime()))
                .setUserId(userDTO.getId())
                .setGroupId(groupId);
        isExist.setCount(isExist.getCount()+1);
        fileMapper.updateById(isExist);
        groupFileMapper.insert(toInsertUploadInfo);


    }

    public FileDTO setFileIcon(FileDTO newFile){
        newFile.setFileType(newFile.getFileType().toLowerCase());
        String type = newFile.getFileType();
        if(type.equals("apk")){
            newFile.setIcon(StaticValue.url + "/icon/file/apk.png");
        } else if(StaticValue.audioPrefixSet.contains(type)){
            newFile.setIcon(StaticValue.url + "/icon/file/audio.png");

        }else if(StaticValue.compressionPrefixSet.contains(type)) {
            newFile.setIcon(StaticValue.url + "/icon/file/compression.png");
        }else if(type.equals("docx")){
            newFile.setIcon(StaticValue.url + "/icon/file/docx.png");
        }else if(type.equals("pdf")){
            newFile.setIcon(StaticValue.url + "/icon/file/pdf.png");
        }else if(type.equals("txt")){
            newFile.setIcon(StaticValue.url + "/icon/file/txt.png");
        }else if(type.equals("xls")){
            newFile.setIcon(StaticValue.url + "/icon/file/xls.png");
        }else{
            newFile.setIcon(StaticValue.url + "/icon/file/other.png");
        }

        return newFile;
    }





}
