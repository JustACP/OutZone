package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.pojo.ContentVO;
import com.outzone.pojo.GroupFileDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GroupFileMapper extends BaseMapper<GroupFileDTO> {
    List<ContentVO> getGroupFileList(String absolutePath, Long groupId);
}
