package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.cache.RedisMybatisCache;
import com.outzone.pojo.FileDTO;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

@Mapper


public interface FileMapper extends BaseMapper<FileDTO> {


}
