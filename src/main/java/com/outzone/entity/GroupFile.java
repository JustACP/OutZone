package com.outzone.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@Accessors(chain = true)
@TableName("group_file")
public class GroupFile {
    long id;
    @TableField("userId")
    long userId;
    @TableField("groupId")
    long groupId;
    @TableField("fileId")
    long fileId;
    @TableField("parentDirectoryId")
    long parentDirectoryId;
    @TableField("absolutePath")
    String absolutePath;
    @TableField("uploadDate")
    Timestamp uploadDate;
    @TableField("fileName")
    String fileName;
}
