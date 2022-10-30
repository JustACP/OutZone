package com.outzone.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.outzone.entity.LoginUser;
import com.outzone.entity.ResponseResult;
import com.outzone.entity.TimeLongValue;
import com.outzone.entity.User;
import com.outzone.mapper.MenuMapper;
import com.outzone.mapper.UserMapper;
import com.outzone.mapper.UserRoleMapper;
import com.outzone.util.JwtUtil;
import com.outzone.util.RedisUtil;
import com.outzone.util.VerifiCodeUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class LoginService{


    
    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private RedisUtil redisUtil;
    @Resource
    UserMapper userMapper;
    @Resource
    MenuMapper menuMapper;
    @Resource
    UserRoleMapper userRoleMapper;
    @Resource
    MailService mailService;
    public ResponseResult register(User registerUser) {
        ResponseResult reigisterResponse = new ResponseResult(HttpStatus.OK.value(),"注册成功");
        if(redisUtil.getCacheObject("registerCode:"+registerUser.getMailAddress())
                != registerUser.getVerificationCode()){
            reigisterResponse.setCode(HttpStatus.PRECONDITION_FAILED.value());
            reigisterResponse.setMsg("验证码错误");
            return reigisterResponse;
        }



        //设置注册用户的基本信息 等待下一步验证

        registerUser.setPassword(new BCryptPasswordEncoder().encode(registerUser.getPassword()));
        Timestamp nowDateTime = new Timestamp(new Date().getTime());
        registerUser.setRegisterTime(nowDateTime);
        registerUser.setStatus(1);
        userMapper.insert(registerUser);
        userRoleMapper.setUserRole(registerUser.getId(),2L);


        //先登陆一手
        UsernamePasswordAuthenticationToken  authenticationToken =
                new UsernamePasswordAuthenticationToken(registerUser.getUsername(), registerUser.getPassword());

        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        LoginUser registerLogin = (LoginUser)  authenticate.getPrincipal();
        registerLogin.getUser().setUUID(UUID.randomUUID().toString().replaceAll("-",""));
        String loginUUID = registerLogin.getUser().getUUID();
        String jwt = JwtUtil.createJWT(loginUUID, TimeLongValue.Month);
        HashMap<String,String> map = new HashMap<>();
        map.put("token", jwt);

        redisUtil.setCacheObject("login:"+loginUUID, JSONObject.toJSONString(registerLogin),30, TimeUnit.DAYS);
        reigisterResponse.setData(map);
        return reigisterResponse;

    }

    public ResponseResult logout() {
        //获取 ContextHolder中的用户 id
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        LoginUser user = (LoginUser) authentication.getPrincipal();
        String userUUID  = user.getUser().getUUID();
        //删除redis中的值
        redisUtil.deleteObject("login:"+userUUID);

        return new ResponseResult(HttpStatus.OK.value(), "登出成功");
    }

    public ResponseResult login(User user) {
        //AuthenticationManager authenticate进行用户认证
        UsernamePasswordAuthenticationToken  authenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());

        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
    
        //如果没通过 给出对应提示
        if(Objects.isNull(authenticate)){
            throw new RuntimeException("登陆失败");
        }
        //认证通过  , 使用userid生成jwt jwt存入ResponseResult
        
        LoginUser loginUser = (LoginUser)  authenticate.getPrincipal();
        loginUser.getUser().setUUID(UUID.randomUUID().toString().replaceAll("-",""));
        String loginUUID = loginUser.getUser().getUUID();
        String jwt = JwtUtil.createJWT(loginUUID, TimeLongValue.Month);
        HashMap<String,String> map = new HashMap<>();
        map.put("token", jwt);
        //把完整的用户信息存入redis


        System.out.println(loginUser);
        redisUtil.setCacheObject("login:"+loginUUID, JSONObject.toJSONString(loginUser),30, TimeUnit.DAYS);

        return new ResponseResult<>(HttpStatus.OK.value(), "登陆成功",map);
    }


    public ResponseResult sendRegisterCode(User registerUser) throws MessagingException, IOException {
        ResponseResult registerCodeResponse = new ResponseResult(HttpStatus.OK.value(),"发送成功");
        //判断邮箱 和 用户名是否被占用
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>();
        queryWrapper.eq(User::getMailAddress,registerUser.getMailAddress())
                .or()
                .eq(User::getUsername,registerUser.getUsername());
        User isExist = userMapper.selectOne(queryWrapper);

        if(!Objects.isNull(isExist)){
            registerCodeResponse.setCode(HttpStatus.CONFLICT.value());

            if(isExist.getUsername().equals(registerUser.getUsername())){
                registerCodeResponse.setMsg("用户名已存在");
            }else{
                registerCodeResponse.setMsg("邮箱已使用");
            }
            return registerCodeResponse;

        }
        String registerCode = VerifiCodeUtil.generateVerifiCode();
        redisUtil.setCacheObject("registerMail:"+registerUser.getMailAddress(),registerCode,5,TimeUnit.MINUTES);
        mailService.sendTemplateMessage("OutZone注册验证码",registerUser.getMailAddress(),registerCode,"新用户");
        return  registerCodeResponse;
    }
}
