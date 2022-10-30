package com.outzone.controller;

import com.outzone.entity.LoginUser;
import com.outzone.entity.ResponseResult;
import com.outzone.entity.User;
import com.outzone.service.LoginService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.io.IOException;

@Controller
@RequestMapping("/user")
public class UserController {
    @Resource
    private LoginService loginService;





    @RequestMapping("/login")
    @ResponseBody
    public ResponseResult login(@RequestBody User user){
        System.out.println(user.toString());

        return loginService.login(user);
    }

    @RequestMapping("/logout")
    @ResponseBody
    public ResponseResult logout(){
        return loginService.logout();
    }


    @RequestMapping(value = "/hello")
//    @PreAuthorize("hasAuthority('public:files:download')")
    @ResponseBody
    public String hello(){
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        LoginUser user  = (LoginUser) authentication.getPrincipal();
        System.out.println(user.getUser().toString());
        return "{msg:\"hello\"}";
    }

    @RequestMapping(value = "/register")
    @ResponseBody
    public ResponseResult register(@RequestBody User registerUser){
        registerUser.setStatus(0).setRole("user");
        return loginService.register(registerUser);

    }

    @PostMapping("/registerCode")
    @ResponseBody
    public ResponseResult verifiCode(@RequestBody User registerUser) throws MessagingException, IOException {
        SecurityContext context = SecurityContextHolder.getContext();
        User loginUser = (User) context.getAuthentication().getPrincipal();
        System.out.println(loginUser.getUsername());

        return loginService.sendRegisterCode(registerUser);
    }

}
