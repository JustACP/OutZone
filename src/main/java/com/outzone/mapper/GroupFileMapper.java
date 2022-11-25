package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.pojo.ContentVO;
import com.outzone.pojo.GroupFileDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupFileMapper extends BaseMapper<GroupFileDTO> {

    void delAllSubGroupFile(@Param(value = "nowPath")String nowPath,
                            @Param(value = "groupId") Long groupId);
    List<ContentVO> getGroupFileList(String absolutePath, Long groupId);
    List<GroupFileDTO> getAllSubGroupFileList(String nowPath, Long groupId);
}
