package com.outzone.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@TableName("directory")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DirectoryDTO {

    @TableId(value = "directoryId" ,type = IdType.ASSIGN_ID)
    Long directoryId;
    @TableField("parentDirectoryId")
    long parentDirectoryId;
    @TableField("ownerId")
    long ownerId;
    String name;
    @TableField("absolutePath")
    String absolutePath;
    @TableField("isGroupDirectory")
    boolean groupDirectory;

    public void setIdAsNull(){
        this.directoryId = null;
    }
}
