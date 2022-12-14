package com.outzone.filter;

import com.alibaba.fastjson2.JSONObject;
import com.outzone.pojo.vo.LoginUserVO;
import com.outzone.util.JwtUtil;
import com.outzone.util.RedisUtil;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Resource
    private RedisUtil redisUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //获取token
        String token = request.getHeader("token");
        if (!StringUtils.hasText(token) || token.equals("undefined")) {
            //放行
            filterChain.doFilter(request, response);
            return;

        }
        //解析token
        String userUUID;
        try {
            Claims tokenClaims = JwtUtil.praseJWT(token);
//            System.out.println(tokenClaims.getSubject());
            userUUID = tokenClaims.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("token illegal");
        }

        //从redis里面获取信息
        String redisKey = "login:" + userUUID;

        LoginUserVO loginUserVO = JSONObject.parseObject(redisUtil.getCacheObject(redisKey), LoginUserVO.class);
        if(Objects.isNull(loginUserVO)){
            throw new RuntimeException("用户未登陆");
        }
        //存入secuirityContextHolder
        //获取权限信息封装到Authentication
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUserVO,null, loginUserVO.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        // 这里放行是因为Context里面已经有了用户信息不用害怕了
        filterChain.doFilter(request,response);
    }
}
