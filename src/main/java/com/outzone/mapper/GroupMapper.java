package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.pojo.GroupsDTO;
import com.outzone.pojo.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GroupMapper extends BaseMapper<GroupsDTO> {
    public UserDTO getGroupMaster(@Param("groupId") Long groupId);
    public void getAdminList(@Param("groupId") Long groupId);
}
