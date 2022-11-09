package com.outzone.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.outzone.entity.*;
import com.outzone.mapper.*;
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
import java.util.List;
import java.util.Objects;

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

    public ResponseResult upload(MultipartFileParams fileParams){

        String filePath = uploadFilePath +"chunk/" +fileParams.getChunkNumber();
        File fileTemp = new File(filePath);

        File parentFile = fileTemp.getParentFile();

        if(!parentFile.exists()){
            parentFile.mkdir();
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

    public ResponseResult<HashMap> uploadCheck(MultipartFileParams fileParams, HttpServletResponse response) throws IOException {

        String fileDir = fileParams.getIdentifier();
        String fileName = fileParams.getFilename();

        // 分片目录
        String chunkPath = uploadFilePath  + fileDir+ "/chunk/";

        //分片目录  对象
        File file = new File(chunkPath);
        List<File> chunkFileList = MergeUtil.chunkFileList(file);

        //合并后路径
        String filePath = uploadFilePath+fileDir+"/"+fileName;
        File fileMergeExist = new File(filePath);

        String[] temp;
        boolean isExist = fileMergeExist.exists();
        if(chunkFileList ==null){
            temp = new String[0];
        }else{
            temp = new String[chunkFileList.size()];
            //如果没有合并后文件，代表没有上传完成
            //没上传完，如果有切片，保存已存在切片列表，否则不保存
            if(!isExist && chunkFileList.size() >0){
                for(int i = 0;i<chunkFileList.size();i++){
                    temp[i] = chunkFileList.get(i).getName();//保存已存在文件列表
                }
            }
        }
        if(fileParams.getChunkNumber() == fileParams.getTotalChunks() && fileParams.getChunkNumber() != 0){
            MergeUtil.mergeFile(uploadFilePath,uploadFilePath+"chunk/",uploadFilePath,fileParams.getFilename());
        }
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("needSkiped",isExist);
        hashMap.put("uploadList",temp);
        response.setStatus(204);
        response.getWriter().write("nofile");
        return new ResponseResult<HashMap>(HttpStatus.OK.value(), "分片检查返回",hashMap);
    }

    public ResponseResult uploadSuccess(UploadFileInfo uploadFileInfo){

        String chunkPath = uploadFilePath+  uploadFileInfo.getUniqueIdentifier()+"chunk/";
        String mergePath = uploadFilePath+ uploadFileInfo.getUniqueIdentifier() + "/";
        File file = MergeUtil.mergeFile(uploadFilePath,chunkPath,mergePath, uploadFileInfo.getName());
        if(file == null){

            return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "文件合并失败");
        }

        return new ResponseResult<>(HttpStatus.OK.value(), "文件合并成功");

    }

    /**
     * 检查文件是否已经存在
     * @param JsonString
     * @includ groupId ，如果为空则为用户文件
     * @include fileMd5
     * @include uploadCloudPath
     * @include fileName
     * @Author re1ife
    **/
    public ResponseResult checkFile(String JsonString){
        String groupId = (String) JSONObject.parseObject(JsonString).get("groupId");
        String fileMd5 = (String) JSONObject.parseObject(JsonString).get("md5");
        String fileName = (String) JSONObject.parseObject(JsonString).get("fileName");
        String uploadCloudPath = (String) JSONObject.parseObject(JsonString).get("uploadCloudPath");
        ResponseResult result = new ResponseResult<>();

        LambdaQueryWrapper<FileDatabase> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FileDatabase::getMd5,fileMd5);
        FileDatabase isExist = fileMapper.selectOne(lambdaQueryWrapper);

        String parentDir = uploadCloudPath.substring(uploadCloudPath.lastIndexOf("/"));
        LambdaQueryWrapper<Directory> directoryWrapper = new LambdaQueryWrapper<>();

        if(Objects.isNull(isExist)){
            result.setCode(HttpStatus.NOT_FOUND.value());
            result.setMsg("文件不存在");
            return result;
        }
        User uploadUser = ((LoginUser)securityContextService.getUserFromContext()).getUser();
        if(groupId.equals("-1")){


            UserPersonalFile toInsertUploadInfo = new UserPersonalFile();

            directoryWrapper.eq(Directory::getName,parentDir)
                    .eq(Directory::isGroupDirectory,false);
            Directory parentDirObj = directoryMapper.selectOne(directoryWrapper);

            toInsertUploadInfo.setFileName(fileName)
                            .setFileId(isExist.getId())
                            .setAbsolutePath(uploadCloudPath)
                            .setParentDirectoryId(parentDirObj.getDirectoryId())
                            .setUploadDate(new Timestamp(new Date().getTime()))
                            .setUserId(uploadUser.getId());
            userFileMapper.insert(toInsertUploadInfo);

        }else{
            GroupFile toInsertUploadInfo = new GroupFile();
            LambdaQueryWrapper<Groups> groupWrapper = new LambdaQueryWrapper<>();

            groupWrapper.eq(Groups::getGroupId,Long.valueOf(groupId)).eq(Groups::getUserId,uploadUser.getId());
            Groups uploadGroup = groupMapper.selectOne(groupWrapper);
            directoryWrapper.eq(Directory::getName,parentDir)
                    .eq(Directory::isGroupDirectory,true);

            Directory parentDirObj = directoryMapper.selectOne(directoryWrapper);
            toInsertUploadInfo.setFileName(fileName)
                    .setFileId(isExist.getId())
                    .setAbsolutePath(uploadCloudPath)
                    .setParentDirectoryId(parentDirObj.getDirectoryId())
                    .setUploadDate(new Timestamp(new Date().getTime()))
                    .setUserId(uploadUser.getId())
                    .setGroupId(uploadGroup.getGroupId());
            groupFileMapper.insert(toInsertUploadInfo);

        }
        result.setCode(HttpStatus.OK.value());
        result.setMsg("文件已保存");
        return result;





    }

}
