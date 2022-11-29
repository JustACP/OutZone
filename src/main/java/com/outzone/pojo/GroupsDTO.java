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
    @TableId(value = "id" ,type = IdType.ASSIGN_ID)
    Long id;
    @TableField("groupId")
    Long groupId;

    @TableField("userId")
    long userId;
    @TableField("groupName")
    String groupName;
    @TableField("isMember")
    Boolean isMemeber;
    @TableField("icon")
    String icon;
    String role;

    public GroupsDTO(Long id, Long groupId, long userId, String groupName, Boolean isMemeber, String role) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.groupName = groupName;
        this.isMemeber = isMemeber;
        this.role = role;
    }
}
