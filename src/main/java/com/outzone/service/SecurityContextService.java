package com.outzone.service;

import com.outzone.pojo.LoginUserVO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecurityContextService {

    public LoginUserVO getUserFromContext(){

        return (LoginUserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public List<String> getUserPrivileges(){
        return getUserFromContext().getPermissions();
    }

}
