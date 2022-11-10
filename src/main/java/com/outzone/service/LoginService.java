package com.outzone.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.outzone.mapper.MenuMapper;
import com.outzone.mapper.UserMapper;
import com.outzone.mapper.UserRoleMapper;
import com.outzone.pojo.LoginUserVO;
import com.outzone.pojo.ResponseResult;
import com.outzone.pojo.TimeLongValue;
import com.outzone.pojo.UserDTO;
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
    public ResponseResult register(UserDTO registerUserDTO) {
        ResponseResult reigisterResponse = new ResponseResult(HttpStatus.OK.value(),"注册成功");
        if(redisUtil.getCacheObject("registerCode:"+ registerUserDTO.getMailAddress())
                != registerUserDTO.getVerificationCode()){
            reigisterResponse.setCode(HttpStatus.PRECONDITION_FAILED.value());
            reigisterResponse.setMsg("验证码错误");
            return reigisterResponse;
        }



        //设置注册用户的基本信息 等待下一步验证
        redisUtil.deleteObject("registerCode:"+ registerUserDTO.getMailAddress());
        registerUserDTO.setPassword(new BCryptPasswordEncoder().encode(registerUserDTO.getPassword()));
        Timestamp nowDateTime = new Timestamp(new Date().getTime());
        registerUserDTO.setRegisterTime(nowDateTime);
        registerUserDTO.setStatus(1);
        userMapper.insert(registerUserDTO);
        userRoleMapper.setUserRole(registerUserDTO.getId(),2L);


        //先登陆一手
        UsernamePasswordAuthenticationToken  authenticationToken =
                new UsernamePasswordAuthenticationToken(registerUserDTO.getUsername(), registerUserDTO.getPassword());

        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        LoginUserVO registerLogin = (LoginUserVO)  authenticate.getPrincipal();
        registerLogin.getUserDTO().setUUID(UUID.randomUUID().toString().replaceAll("-",""));
        String loginUUID = registerLogin.getUserDTO().getUUID();
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
        LoginUserVO user = (LoginUserVO) authentication.getPrincipal();
        String userUUID  = user.getUserDTO().getUUID();
        //删除redis中的值
        redisUtil.deleteObject("login:"+userUUID);

        return new ResponseResult(HttpStatus.OK.value(), "登出成功");
    }

    public ResponseResult login(UserDTO userDTO) {
        //AuthenticationManager authenticate进行用户认证
        UsernamePasswordAuthenticationToken  authenticationToken = new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getPassword());

        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
    
        //如果没通过 给出对应提示
        if(Objects.isNull(authenticate)){
            throw new RuntimeException("登陆失败");
        }
        //认证通过  , 使用userid生成jwt jwt存入ResponseResult
        
        LoginUserVO loginUserVO = (LoginUserVO)  authenticate.getPrincipal();
        loginUserVO.getUserDTO().setUUID(UUID.randomUUID().toString().replaceAll("-",""));
        String loginUUID = loginUserVO.getUserDTO().getUUID();
        String jwt = JwtUtil.createJWT(loginUUID, TimeLongValue.Month);
        HashMap<String,String> map = new HashMap<>();
        map.put("token", jwt);
        //把完整的用户信息存入redis


        System.out.println(loginUserVO);
        redisUtil.setCacheObject("login:"+loginUUID, JSONObject.toJSONString(loginUserVO),30, TimeUnit.DAYS);

        return new ResponseResult<>(HttpStatus.OK.value(), "登陆成功",map);
    }


    public ResponseResult sendRegisterCode(UserDTO registerUserDTO) throws MessagingException, IOException {
        ResponseResult registerCodeResponse = new ResponseResult(HttpStatus.OK.value(),"发送成功");
        //判断邮箱 和 用户名是否被占用
        LambdaQueryWrapper<UserDTO> queryWrapper = new LambdaQueryWrapper<UserDTO>();
        queryWrapper.eq(UserDTO::getMailAddress, registerUserDTO.getMailAddress())
                .or()
                .eq(UserDTO::getUsername, registerUserDTO.getUsername());
        UserDTO isExist = userMapper.selectOne(queryWrapper);

        if(!Objects.isNull(isExist)){
            registerCodeResponse.setCode(HttpStatus.CONFLICT.value());

            if(isExist.getUsername().equals(registerUserDTO.getUsername())){
                registerCodeResponse.setMsg("用户名已存在");
            }else{
                registerCodeResponse.setMsg("邮箱已使用");
            }
            return registerCodeResponse;

        }
        String registerCode = VerifiCodeUtil.generateVerifiCode();
        redisUtil.setCacheObject("registerMail:"+ registerUserDTO.getMailAddress(),registerCode,5,TimeUnit.MINUTES);
        mailService.sendTemplateMessage("OutZone注册验证码", registerUserDTO.getMailAddress(),registerCode,"新用户");
        return  registerCodeResponse;
    }
}
