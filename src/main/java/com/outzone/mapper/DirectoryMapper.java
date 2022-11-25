package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.pojo.ContentVO;
import com.outzone.pojo.DirectoryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

@Mapper
public interface DirectoryMapper extends BaseMapper<DirectoryDTO> {
    List<ContentVO> getDirList( String parentDirectory, Long ownerId, boolean isGroup);

    List<ContentVO> getDirListById(List<Long> id);
    List<DirectoryDTO> getAllSubDir(@Param(value = "nowPath")String nowPath,
                                    @Param(value = "isGroup")boolean isGroup,
                                    @Param(value = "ownerId") Long ownerId);

    void delAllSubDir(@Param(value = "nowPath")String nowPath,
                                    @Param(value = "isGroup")boolean isGroup,
                                    @Param(value = "ownerId") Long ownerId);
}
