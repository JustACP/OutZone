package com.outzone.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName(value = "share")
public class ShareDTO {
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    Long id;
    @TableField(value = "shareId")
    String shareId;
    @TableField(value = "userId")
    Long UserId;
    @TableField(value = "isDirectory")
    boolean isDirectory;
    @TableField(value = "password")
    String passwords;
    String url;
    boolean enable = true;
    @TableField(value = "fileOrDirectoryId")
    Long fileOrDirectoryId;



}
