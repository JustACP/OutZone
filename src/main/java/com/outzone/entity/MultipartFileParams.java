package com.outzone.entity;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MultipartFileParams {
    private int chunkNumber;
    private long chunkSize;
    private long currentChunkSize;
    private long totalSize;
    private String identifier;
    private String filename;
    private String relativePath;
    private int totalChunks;
    //接受文件
    private MultipartFile file;
}
