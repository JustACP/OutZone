package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.pojo.GroupsDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupMapper extends BaseMapper<GroupsDTO> {
}
