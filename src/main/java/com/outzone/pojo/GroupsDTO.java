package com.outzone.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("groups")
public class GroupsDTO {
    @TableField("groupId")
    long groupId;

    @TableField("userId")
    long userId;
    @TableField("groupName")
    String groupName;

    String role;
}
