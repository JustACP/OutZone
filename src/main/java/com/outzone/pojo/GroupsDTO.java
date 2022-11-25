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
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("groups")
public class GroupsDTO {
    @TableId(value = "groupId" ,type = IdType.ASSIGN_ID)
    Long groupId;

    @TableField("userId")
    long userId;
    @TableField("groupName")
    String groupName;

    String role;
}
