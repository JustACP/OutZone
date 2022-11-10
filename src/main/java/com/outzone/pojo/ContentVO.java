package com.outzone.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class ContentVO {
    boolean directoryType;
    String name;
    String size;
    String type;
    //文件或者目录Id
    long id;
    long parentId;


    public ContentVO(boolean directoryType, String name, long id, long parentId){
        this.directoryType = true;
        this.name = name;
        this.id = id;
        this.parentId = parentId;
    }
    public ContentVO(String name,String size,String type,long id,long parentId){
        this.directoryType = false;
        this.size = size;
        this.type = type;
        this.name = name;
        this.id = id;
        this.parentId = parentId;
    }



}
