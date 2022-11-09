package com.outzone.service;

import com.outzone.entity.LoginUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityContextService {

    public LoginUser getUserFromContext(){
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
