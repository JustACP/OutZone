package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.cache.RedisMybatisCache;
import com.outzone.entity.User;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
@Mapper
@CacheNamespace(implementation = RedisMybatisCache.class)
public interface UserMapper extends BaseMapper<User> {
    @Select("select * from user where username= #{username}")
    User getUserByUsername(String username);
}