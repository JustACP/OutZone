package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.entity.UserPersonalFile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserFileMapper extends BaseMapper<UserPersonalFile> {
}
