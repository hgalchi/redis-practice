package com.spartacoding.msa.eurekaclient.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> T getValueAsClass(String key, Class<T> clazz) {
        return objectMapper.convertValue(redisTemplate.opsForValue().get(key), clazz);
    }

    public void setValueWithExpire(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    public void setValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void setIfPresent(String key, Object value) {
        redisTemplate.opsForValue().setIfPresent(key, value);
    }

    public void setExpire(String key, long timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }


    public Set<Object> hasKeys(String key) {
        Set<Object> values = new HashSet<>();

        ScanOptions options = ScanOptions.scanOptions()
            .match(key)
            .count(100)
            .build();

        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan(options)) {
            while (cursor.hasNext()) {
                String cursorKey = new String(cursor.next());
                Object value = redisTemplate.opsForValue().get(cursorKey);
                if (value != null) {
                    values.add(value);
                }
            }
        } catch (Exception e) {

        }

        return values;
    }

    public void deleteValue(Set<String> keys) {
        redisTemplate.unlink(keys);
    }

}
