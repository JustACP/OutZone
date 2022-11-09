package com.outzone.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("groups")
public class Groups {
    @TableField("groupId")
    long groupId;

    @TableField("userId")
    long userId;
    @TableField("groupName")
    String groupName;
}
