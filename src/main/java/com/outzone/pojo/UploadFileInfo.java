package com.outzone.pojo;

import lombok.Data;

@Data
public class UploadFileInfo {


    private long totalSize;
    private String identifier;
    private String filename;

    private int totalChunks;
    private String uploadCloudPath;
    private Long groupId;
}
