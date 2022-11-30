package com.outzone.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("file")
@Accessors(chain = true)
public class FileDTO {
    @TableId(value = "id", type = IdType.INPUT)
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
    @TableField("icon")
    String icon;

}
