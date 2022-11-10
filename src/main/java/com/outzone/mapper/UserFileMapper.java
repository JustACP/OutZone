package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.pojo.ContentVO;
import com.outzone.pojo.UserFileDTO;
import org.apache.ibatis.annotations.Mapper;


import java.util.List;

@Mapper
public interface UserFileMapper extends BaseMapper<UserFileDTO> {

    List<ContentVO> getUserFileList(String absolutePath, Long userId);

}
