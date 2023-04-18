package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.cache.RedisMybatisCache;
import com.outzone.pojo.vo.ContentVO;
import com.outzone.pojo.UserFileDTO;
import org.apache.ibatis.annotations.CacheNamespace;
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
    List<ContentVO> searchFilesByName(Long userId, String fileName);
    List<ContentVO> groupByType(Long userId,@Param("prefix") List<String> prefix);
    List<ContentVO> groupExcludeType(Long userId,List<String> prefix);

    //主要想尝试一下 sum和distinct配合使用
    Long getUserStorageCapacity(Long userId);

}
