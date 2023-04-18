package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.cache.RedisMybatisCache;
import com.outzone.pojo.FriendsDTO;
import com.outzone.pojo.UserDTO;
import com.outzone.pojo.vo.FriendsVO;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper

public interface UserMapper extends BaseMapper<UserDTO> {


    List<FriendsVO> searchUserByUsername(@Param("username") String username,@Param("userId") Long userId);
}
