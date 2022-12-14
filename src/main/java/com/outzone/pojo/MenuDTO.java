package com.outzone.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "sys_menu")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuDTO implements Serializable {
    private  static final long serialVersionUID = 200;

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;
    private  String menuName;

    private String path;
    private String component;
    private String visible;
    private String status;
    private  String perms;
    private String icon;
    private  Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;
    private Integer delFlag;
    private String remark;

}
