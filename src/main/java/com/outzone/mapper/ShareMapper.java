package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.cache.RedisMybatisCache;
import com.outzone.pojo.ShareDTO;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

@Mapper

public interface ShareMapper extends BaseMapper<ShareDTO> {
}
