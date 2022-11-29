package com.outzone.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "friends")
public class FriendsDTO {
    @TableId (value = "id",type = IdType.ASSIGN_ID)
    Long id;
    @TableField(value = "inviteUserId")
    Long inviteId;
    @TableField(value = "invitedUserId")
    Long invitedId;
    Timestamp time;
    @TableField(value = "isFriend")
    Boolean isFriend;
}
