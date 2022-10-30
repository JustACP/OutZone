package com.outzone.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
@Component
public class RedisUtil {
    @Resource
    RedisTemplate<String,String> redisTemplate;

    public  void setCacheObject(String key,String value)
    {
        redisTemplate.opsForValue().set(key, value);
    }

    public  void setCacheObject(String Key,String value, Integer timeout, TimeUnit timeUnit){
        redisTemplate.opsForValue().set(Key, value,timeout,timeUnit);
    }

    public boolean expire( String key,  long timeout){
        return expire(key, timeout,TimeUnit.SECONDS);
    }

    public boolean expire( String key,  long timeout, TimeUnit unit){
        return redisTemplate.expire(key, timeout,unit);
    }

    public String  getCacheObject ( String key){

        return  redisTemplate.opsForValue().get(key);
    }

    public boolean deleteObject( String key){
        return redisTemplate.delete(key);
    }

    public long deleteObject ( Collection collection){
        return redisTemplate.delete(collection);
    }



}
