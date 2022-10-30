package com.outzone.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper

public interface UserRoleMapper{
    @Insert("insert into sys_user_role (user_id,role_id) values (#{userid}, #{roleid}))")
    int setUserRole(long userid,long roleid);
}
