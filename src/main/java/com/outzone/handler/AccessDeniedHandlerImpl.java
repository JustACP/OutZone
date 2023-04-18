package com.outzone.handler;

import com.alibaba.fastjson2.JSON;
import com.outzone.pojo.ResponseResult;
import com.outzone.util.ResponseUtill;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ResponseResult authDeniedResult = new ResponseResult(HttpStatus.FORBIDDEN.value(), "无权访问");
        String responseString = JSON.toJSONString(authDeniedResult);

        ResponseUtill.renderString(response,responseString);
    }
}
