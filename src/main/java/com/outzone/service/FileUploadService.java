package com.outzone.service;

import com.outzone.entity.FileInfo;
import com.outzone.entity.MultipartFileParams;
import com.outzone.entity.ResponseResult;
import com.outzone.util.MergeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Service
public class FileUploadService{
    @Value("${upload.file.path}")
    private String uploadFilePath;

    public ResponseResult upload(MultipartFileParams fileParams){

        String filePath = uploadFilePath + fileParams.getFilename();
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

    public ResponseResult<HashMap> uploadCheck(MultipartFileParams fileParams) {

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
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("needSkiped",isExist);
        hashMap.put("uploadList",temp);
        return new ResponseResult<HashMap>(HttpStatus.OK.value(), "分片检查返回",hashMap);
    }

    public ResponseResult uploadSuccess(FileInfo fileInfo){
        String chunkPath = uploadFilePath+  fileInfo.getUniqueIdentifier()+"/chunk/";
        String mergePath = uploadFilePath+ fileInfo.getUniqueIdentifier() + "/";
        File file = MergeUtil.mergeFile(uploadFilePath,chunkPath,mergePath,fileInfo.getName());
        if(file == null){
            return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "文件合并失败");
        }
        return new ResponseResult<>(HttpStatus.OK.value(), "文件合并成功");

    }


//    public  ResponseResult upload(MultipartFileParams fileParams){
//        String fileDir = fileParams.getIdentifier();
//        int chunkNumber = fileParams.getChunkNumber();
//        String filePath = uploadFilePath + fileDir + "/chunk/"+chunkNumber;
//        File fileTemp = new File(filePath);
//        File parentFile = fileTemp.getParentFile();
//        if(!parentFile.exists()){
//            parentFile.mkdirs();
//        }
//        try{
//            MultipartFile file = fileParams.getFile();
//            file.transferTo(fileTemp);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return new ResponseResult(HttpStatus.OK.value(),"分片上传成功")
//    }
}
