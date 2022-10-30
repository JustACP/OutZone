package com.outzone.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails{
    private User user;


    //储存权限信息
    @JSONField(serialize = false)
    private List<GrantedAuthority> authorities;

    private List<String> permissions;


    public LoginUser(User user,List<String> permissions) {
        this.user = user;
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


        return user.getPassword();
    }

    @Override
    public String getUsername() {

        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {

        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        return true;
    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return true;
    }



}
