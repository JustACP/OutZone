package com.outzone.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("file")
public class FileDatabase {
    long id;
    String filename;
    long count;
    String md5;
    @TableField("physicalPath")
    String physicalPath;
    @TableField("fileType")
    String fileType;
    Timestamp uploadtime;
    @TableField("fileSize")
    long fileSize;

}
