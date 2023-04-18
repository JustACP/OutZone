package com.outzone.pojo.vo;

import com.outzone.pojo.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserVO {
    Long id;
    String username;
    String icon;
    Timestamp registerTime;

    public static UserVO convertByUserDTO(UserDTO user) {
        return new UserVO(user.getId(), user.getUsername(), user.getIcon(), user.getRegisterTime());

    }
}
