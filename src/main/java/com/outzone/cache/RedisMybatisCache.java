package com.outzone.cache;

import org.apache.ibatis.cache.Cache;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisMybatisCache implements Cache {

    public final String id;
    public static RedisTemplate<Object,Object> template;

    public RedisMybatisCache(String id) {
        this.id = id;
    }
    public static void setTemplate(RedisTemplate<Object,Object> template){
        RedisMybatisCache.template = template;
    }
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void putObject(Object o, Object o1) {
        template.opsForValue().set(o,o1);
    }

    @Override
    public Object getObject(Object o) {
        return template.opsForValue().get(o);
    }

    @Override
    public Object removeObject(Object o) {
        return template.delete(o);
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
