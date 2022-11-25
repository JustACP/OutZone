package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.pojo.ContentVO;
import com.outzone.pojo.UserFileDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFileMapper extends BaseMapper<UserFileDTO> {
    void delAllSubUserFile(@Param(value = "nowPath")String nowPath,
                            @Param(value = "userId") Long userId);


    List<UserFileDTO> getAllSubUserFileList(String nowPath, Long userId);
    List<ContentVO> getUserFileList(String absolutePath, Long userId);
    List<ContentVO> getUserFileListById(List<Long> id);

}
