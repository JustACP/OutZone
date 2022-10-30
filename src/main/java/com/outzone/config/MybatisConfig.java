package com.outzone.config;

//import com.outzone.cache.RedisMybatisCache;

import com.outzone.cache.RedisMybatisCache;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Configuration

public class MybatisConfig {

    @Resource
    RedisTemplate<String ,Object> template;
    // 设置Redis模板

    //传入RedisMybatis缓存
    @PostConstruct
    public void initRedisMybatisCache(){
        RedisMybatisCache.setTemplate(template);
    }


}
