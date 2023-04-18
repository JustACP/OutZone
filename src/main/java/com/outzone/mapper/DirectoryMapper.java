package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.cache.RedisMybatisCache;
import com.outzone.pojo.vo.ContentVO;
import com.outzone.pojo.DirectoryDTO;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper

public interface DirectoryMapper extends BaseMapper<DirectoryDTO> {
    List<ContentVO> getDirList(@Param("parentDirectory") String parentDirectory, Long ownerId, boolean isGroup);

    List<ContentVO> getDirListById(List<Long> id);
    List<DirectoryDTO> getAllSubDir(@Param(value = "nowPath")String nowPath,
                                    @Param(value = "isGroup")boolean isGroup,
                                    @Param(value = "ownerId") Long ownerId);

    void delAllSubDir(@Param(value = "nowPath")String nowPath,
                                    @Param(value = "isGroup")boolean isGroup,
                                    @Param(value = "ownerId") Long ownerId);

    List<ContentVO> searchDirByName(@Param("ownerId") Long ownerId, @Param("fileName") String fileName,@Param("isGroup")boolean isGroup);
}
