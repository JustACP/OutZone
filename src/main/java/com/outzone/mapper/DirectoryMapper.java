package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.pojo.ContentVO;
import com.outzone.pojo.DirectoryDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DirectoryMapper extends BaseMapper<DirectoryDTO> {
    List<ContentVO> getDirList(String parentDirectory, Long ownerId,boolean isGroup);

}
