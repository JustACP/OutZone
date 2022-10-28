package com.outzone.cache;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@SuppressWarnings(value = {"unchecked","rawtypes"})
@Component
public class InfoInRedisCache {
    @Resource
    public RedisTemplate redisTemplate;


    /**缓存基本对象 ，Inter String等实体类
    * @param key 
    * @param value
    **/
    public <T> void setCacheObject(final String key, final T value)
    {
        redisTemplate.opsForValue().set(key, value);
    }


    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     * @param timeout 时间
     * @param timeUnit 时间颗粒度
     */
    public <T> void setCacheObject(final String Key,final T value, final Integer timeout, final TimeUnit timeUnit){
        redisTemplate.opsForValue().set(Key, value,timeout,timeUnit);
    }

    /**
     * 设置有效时间
     *
     * @param key Redis键
     * @param timeout 超时时间
     * @param seconds
     * @return true=设置成功；false=设置失败
     */

     public boolean expire(final String key, final long timeout){
        return expire(key, timeout,TimeUnit.SECONDS);
     }

     public boolean expire(final String key, final long timeout,final TimeUnit unit){
        return redisTemplate.expire(key, timeout,unit);
     }

     public <T> T getCacheObject (final String key){
        ValueOperations<String,T> operations = redisTemplate.opsForValue();
        return operations.get(key);
     }

     public boolean deleteObject(final String key){
        return redisTemplate.delete(key);
     }

     public long deleteObject(final Collection collection){
        return redisTemplate.delete(collection);
     }

     public <T> long setCacheList(final String key,final List<T> dataList){
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null  ? 0 : count;
     }

     public <T> List<T> getCacheList(final String key){
        return redisTemplate.opsForList().range(key, 0, -1);
     }

     public <T> BoundSetOperations<String,T> setCacheSet(final String key,final Set<T> dataSet){
        BoundSetOperations<String,T> setOperations = redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while(it.hasNext()){
            setOperations.add(it.next());

        }
        return setOperations;
        
     }

     public <T> Set<T> getCacheSet(final String key){
        return redisTemplate.opsForSet().members(key);
     }

     public <T> void setCacheMap(final String key,final Map<String,T> dataMap){
        if(dataMap != null){
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
     }

     public <T> Map<String, T> getCacheMap(final String key)
    {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value)
    {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey)
    {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 删除Hash中的数据
     * 
     * @param key
     * @param hkey
     */
    public void delCacheMapValue(final String key, final String hkey)
    {
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.delete(key, hkey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys)
    {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern)
    {
        return redisTemplate.keys(pattern);
    }

}
