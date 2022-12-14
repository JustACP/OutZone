package com.outzone.pojo.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.outzone.pojo.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class LoginUserVO implements UserDetails{
    private UserDTO userDTO;


    //储存权限信息
    @JSONField(serialize = false)
    private List<GrantedAuthority> authorities;

    private List<String> permissions;


    public LoginUserVO(UserDTO userDTO, List<String> permissions) {
        this.userDTO = userDTO;
        this.permissions = permissions;
    }


    // 权限
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        if(authorities != null){
            return authorities;
        }
        authorities = permissions.stream()
                      .map(SimpleGrantedAuthority::new)
                      .collect(Collectors.toList());
        return authorities;

    }

    @Override
    public String getPassword() {


        return userDTO.getPassword();
    }

    @Override
    public String getUsername() {

        return userDTO.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {

        return true;
    }

    @Override
    public boolean isAccountNonLocked() {

        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        return true;
    }

    @Override
    public boolean isEnabled() {

        return true;
    }



}
