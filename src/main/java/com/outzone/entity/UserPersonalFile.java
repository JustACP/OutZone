package com.outzone.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@TableName("user_file")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserPersonalFile {
    long id;
    @TableField("userId")
    long userId;
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
