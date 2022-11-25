package com.outzone.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.outzone.mapper.MenuMapper;
import com.outzone.mapper.UserMapper;
import com.outzone.pojo.LoginUserVO;
import com.outzone.pojo.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
@Service
@Slf4j
@Transactional
public class UserDetialServiceImpl implements UserDetailsService{
    @Resource
    UserMapper userMapper;

    @Resource

    MenuMapper menuMapper;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        System.out.println("start login");

        LambdaQueryWrapper<UserDTO>   queryWrapper = new LambdaQueryWrapper<UserDTO>();
        queryWrapper.eq(UserDTO::getUsername,username);
        //查询用户信息

        UserDTO toAuthUserDTO = userMapper.selectOne(queryWrapper);
        log.info(toAuthUserDTO.toString());
        if(Objects.isNull(toAuthUserDTO)){
            throw new RuntimeException("用户名或密码错误");
        }

        //查询对应权限信息
        List<String> listPerms = menuMapper.selectPermsByUserId(toAuthUserDTO.getId());
        //把数据封装成为UserDetials返回
        return new LoginUserVO(toAuthUserDTO,listPerms);

    }
    
    
    
}
