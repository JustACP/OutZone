package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.cache.RedisMybatisCache;
import com.outzone.pojo.GroupsDTO;
import com.outzone.pojo.UserDTO;
import com.outzone.pojo.vo.FriendsVO;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

@Mapper

public interface GroupMapper extends BaseMapper<GroupsDTO> {
    public UserDTO getGroupMaster(@Param("groupId") Long groupId);
    public void getAdminList(@Param("groupId") Long groupId);

    public List<UserDTO> getGroupUserList(@Param("groupId") Long groupId);
}
