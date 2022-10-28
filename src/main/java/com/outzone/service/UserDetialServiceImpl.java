package com.outzone.service;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.outzone.mapper.MenuMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.outzone.entity.LoginUser;
import com.outzone.entity.User;
import com.outzone.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
@Service
public class UserDetialServiceImpl implements UserDetailsService{
    @Resource
    UserMapper userMapper;

    @Resource
    MenuMapper menuMapper;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("start login");

        LambdaQueryWrapper<User>   queryWrapper = new LambdaQueryWrapper<User>();
        queryWrapper.eq(User::getUsername,username);
        //查询用户信息

        User toAuthUser = userMapper.selectOne(queryWrapper);
        System.out.println(toAuthUser);
        if(Objects.isNull(toAuthUser)){
            throw new RuntimeException("用户名或密码错误");
        }

        //TODO 查询对应权限信息
        List<String> listPerms = menuMapper.selectPermsByUserId(toAuthUser.getId());
        //把数据封装成为UserDetials返回
        return new LoginUser(toAuthUser,listPerms);

    }
    
    
    
}
