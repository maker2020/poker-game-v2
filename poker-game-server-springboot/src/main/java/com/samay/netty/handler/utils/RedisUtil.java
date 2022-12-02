package com.samay.netty.handler.utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    /**
     * 根据key获取value
     * 
     * @param key
     * @return
     */
    public Object get(String key){
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 存入String,String键值对
     * 
     * @param key
     * @param value
     */
    public void set(String key,String value){
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 存入String,String键值对，有效期expire(s)
     * 
     * @param key
     * @param value
     * @param expire
     */
    public void set(String key,String value,Integer expire){
        redisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
    }

    /**
     * 删除key(包括value)
     * 
     * @param key
     */
    public void delete(String key){
        redisTemplate.opsForValue().getOperations().delete(key);
    }

    /**
     * 存入hash结构的值,key-(hashkey-value)的hash结构值<p>
     * 默认过期时间2小时
     * 
     * @param key
     * @param filed
     * @param domain
     */
    public void hset(String key,String hashKey,Object value){
        redisTemplate.opsForHash().put(key, hashKey, value);
    }
    
    /**
     * 存入hash结构的值,key-(hashkey-value)<p>
     * 指定过期时间expire(s)
     * 
     * @param key
     * @param hashKey
     * @param value
     * @param expire
     */
    public void hset(String key,String hashKey,Object value,Integer expire){
        redisTemplate.opsForHash().put(key, hashKey, value);
        redisTemplate.expire(key,expire,TimeUnit.SECONDS);
    }

    /**
     * 存入hash表，即Map<?,?>,即同于多个hashKey-Value
     * 
     * @param key
     * @param hashTable
     */
    public void hset(String key,Map<String,Object> hashTable){
        redisTemplate.opsForHash().putAll(key, hashTable);
    }

    /**
     * 如果不存在则存入key-(hashkey-value)
     * 
     * @param key
     * @param hashKey
     * @param value
     */
    public void hsetIfAbsent(String key,String hashKey,Object value){
        redisTemplate.opsForHash().putIfAbsent(key, hashKey, value);
    }

    /**
     * 查询key-hashKey所对应的value
     * 
     * @param key
     * @param hashKey
     * @return
     */
    public Object hget(String key,String hashKey){
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * 判断是否存在该key-hashKey
     * 
     * @param key
     * @param value
     * @return
     */
    public Boolean hasKey(String key,String hashKey){
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }
    
    /**
     * 判断是否存在该key
     * 
     * @param key
     * @return
     */
    public Boolean hasKey(String key){
        return redisTemplate.opsForHash().getOperations().hasKey(key);
    }

    /**
     * 将value左插入对应的list
     * 
     * @param key
     * @param value
     */
    public void pushListL(String key,String value){
        redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 弹出对应的全范围/range的list
     * 
     * @param key
     * @param value
     */
    public List<String> popList(String key){
        return redisTemplate.opsForList().range(key, 0, -1);
    }

}
