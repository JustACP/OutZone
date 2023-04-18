package com.outzone.mapper;

import com.outzone.cache.RedisMybatisCache;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper

public interface UserRoleMapper{
    @Insert("insert into sys_user_role (user_id,role_id) values (#{userId}, #{roleId})")
    int setUserRole(long userId,long roleId);
}
