package com.outzone.service;

import com.outzone.pojo.LoginUserVO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityContextService {

    public LoginUserVO getUserFromContext(){
        return (LoginUserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
