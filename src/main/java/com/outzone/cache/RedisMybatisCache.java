package com.outzone.cache;

import org.apache.ibatis.cache.Cache;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

public class RedisMybatisCache implements Cache {

    public final String id;
    @Resource
    public static RedisTemplate<String,Object> template;

    public RedisMybatisCache(String id) {
        this.id = id;
    }
    public static void setTemplate(RedisTemplate<String,Object> template){
        RedisMybatisCache.template = template;
    }
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void putObject(Object o, Object o1) {
        template.opsForValue().set((String) o,o1);
    }

    @Override
    public Object getObject(Object o) {
        return template.opsForValue().get(o);
    }

    @Override
    public Object removeObject(Object o) {
        return template.delete((String) o);
    }

    @Override
    public void clear() {

        template.execute((RedisCallback<Void>) connection ->{
            connection.flushDb();
            return null;
        });
    }

    @Override
    public int getSize() {
        return 0;
    }
}
