package com.outzone.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MultipartFileParamsVO {
    private int chunkNumber;
    private long chunkSize;
    private long currentChunkSize;
    private String relativePath;
    private long totalSize;
    private String identifier;
    private String filename;

    private int totalChunks;
    private String uploadCloudPath;
    private long groupId;
    //接受文件
    private MultipartFile file;

    public MultipartFileParamsVO setByUploadFileInfo(UploadFileInfo fileInfo){
        this.totalChunks = fileInfo.getTotalChunks();
        this.filename = fileInfo.getFilename();
        this.identifier = fileInfo.getIdentifier();
        this.groupId = fileInfo.getGroupId();
        this.totalSize = fileInfo.getTotalSize();
        this.uploadCloudPath = fileInfo.getUploadCloudPath();
        return  this;
    }

}
