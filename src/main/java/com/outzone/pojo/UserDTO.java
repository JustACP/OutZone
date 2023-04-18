package com.outzone.pojo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName(value = "user")
public class UserDTO implements Serializable {



    @TableId(value = "id", type = IdType.INPUT)
    Long id;
    @TableField(value = "username")
    String username;
    @TableField(value = "password")
    String password;
    
    int status;
    @TableField(value = "mailAddress")
    String mailAddress;
    @TableField(value = "registerTime")
    Timestamp registerTime;
    @TableField(value = "role")
    String role;
    String icon;

    @TableField(exist = false)
    private String UUID;

    @TableField(exist = false)
    private String verificationCode;

    @Override
    public String toString(){
        return "User{id='"+id+"\', name='"+username+"\', password='" +password+"\'";
    }

}
