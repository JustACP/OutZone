// package com.fileshare.service;
//
// import com.fileshare.entity.User;
// import com.fileshare.mapper.UserMapper;
// import com.outzone.mapper.UserMapper;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.stereotype.Service;
//
// import javax.annotation.Resource;
// @Service
// public class AuthService implements UserDetailsService {
//
//     @Resource
//     UserMapper userMapper;
//
//     @Override
//     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//         System.out.println("开始验证");
//         User toLoginUserLogin = userMapper.getUserDetailByUsername(username);
//         System.out.println(toLoginUserLogin.toString());
//
//         if(toLoginUserLogin == null) throw  new UsernameNotFoundException("无此用户");
//         else{
//             return org.springframework.security.core.userdetails.User
//                     .withUsername(toLoginUserLogin.getUsername())
//                     .password(toLoginUserLogin.getPassword())
//                     .roles(toLoginUserLogin.getRole())
//                     .build();
//         }
//
//     }
// }
