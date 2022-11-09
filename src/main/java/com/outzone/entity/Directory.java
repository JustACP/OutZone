package com.outzone.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("directory")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Directory {
    @TableField("directoryId")
    long directoryId;
    @TableField("parentDirectoryId")
    long parentDirectoryId;
    @TableField("ownerId")
    long ownerId;
    String name;
    @TableField("absolutePath")
    String absolutePath;
    @TableField("isGroupDirectory")
    boolean groupDirectory;
}
