package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.pojo.MenuDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper

public interface MenuMapper extends BaseMapper<MenuDTO> {


    List<String> selectPermsByUserId(Long userId);
}
