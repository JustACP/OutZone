package com.outzone.controller;

import com.outzone.mapper.UserFileMapper;
import com.outzone.pojo.*;
import com.outzone.pojo.vo.LoginUserVO;
import com.outzone.service.LoginService;
import com.outzone.service.SecurityContextService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

@Controller
@RequestMapping("/api/user")

public class UserController {
    @Resource
    private LoginService loginService;

    @Resource
    SecurityContextService securityContextService;
    @Resource
    UserFileMapper userFileMapper;




    @RequestMapping("/login")
    @ResponseBody
    public ResponseResult login(@RequestBody UserDTO userDTO, HttpServletRequest request, HttpServletResponse response){
        String token  = request.getHeader("token");
        if(!Objects.isNull(token)){
            SecurityContext context = SecurityContextHolder.getContext();
            LoginUserVO isLogined = (LoginUserVO) context.getAuthentication().getPrincipal();
            if(!isLogined.getPermissions().isEmpty()) return new ResponseResult(HttpStatus.FORBIDDEN.value(), "禁止再次登陆");
        }

        return loginService.login(userDTO);
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
        LoginUserVO user  = (LoginUserVO) authentication.getPrincipal();
//        System.out.println(user.getUserDTO().toString());
        return "{msg:\"hello\"}";
    }

    @RequestMapping(value = "/register")
    @ResponseBody
    public ResponseResult register(@RequestBody UserDTO registerUserDTO){
        registerUserDTO.setStatus(0).setRole("user");
        return loginService.register(registerUserDTO);

    }

    @PostMapping("/registerCode")
    @ResponseBody
    public ResponseResult verifiCode(@RequestBody UserDTO registerUserDTO) throws MessagingException, IOException {
        SecurityContext context = SecurityContextHolder.getContext();
        UserDTO loginUserDTO = (UserDTO) context.getAuthentication().getPrincipal();
//        System.out.println(loginUserDTO.getUsername());

        return loginService.sendRegisterCode(registerUserDTO);
    }

    @GetMapping("/getCapacity")
    @ResponseBody
    public ResponseResult getCapacity(){
        UserDTO requestUser =  securityContextService.getUserFromContext().getUserDTO();
        ResponseResult ok = new ResponseResult(HttpStatus.OK.value(),"已用空间");
        ResponseResult forbidden= new ResponseResult(HttpStatus.FORBIDDEN.value(), "无权访问");
        if(Objects.isNull(requestUser)){
            return forbidden;
        }
        HashMap<String,Long> capacity = new HashMap<>();
        capacity.put("used", userFileMapper.getUserStorageCapacity(requestUser.getId()));
        capacity.put("total", StaticValue.userTotalCapacity);
        ok.setData(capacity);
        return ok;

    }


}
