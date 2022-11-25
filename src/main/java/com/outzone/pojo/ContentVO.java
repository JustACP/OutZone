package com.outzone.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Value;

import java.sql.Date;
import java.sql.Timestamp;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ContentVO {
    boolean directoryType;
    String name;
    Long size;
    String type;
    //文件或者目录Id
    long id;
    long parentId;
    String path;
    String icon;
    Date uploadDate;


    public ContentVO(Long id, String name,  Long parentId,String path){



        this.directoryType = true;
        this.name = name;
        this.id = id;
        this.parentId = parentId;
        this.path = path;
        this.icon = StaticValue.directoryIcon;
    }
    public ContentVO(String name,Long size,String type,Long id,Long parentId,String path,Date uploadDate,String icon){
        this.directoryType = false;
        this.size = size;
        this.type = type;
        this.name = name;
        this.id = id;
        this.parentId = parentId;
        this.path = path;
        this.uploadDate = uploadDate;
        this.icon = icon;
    }



}
