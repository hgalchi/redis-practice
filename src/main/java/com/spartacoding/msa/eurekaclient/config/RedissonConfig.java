package com.spartacoding.msa.eurekaclient.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {


    private static final String REDISSON_HOST_PREFIX = "redis://";

    //redis서버에 RedissonClient를 등록
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress(REDISSON_HOST_PREFIX + "localhost:6379");
        config.useSingleServer().setPassword("systempass");
        config.useSingleServer().setUsername("default");
        return Redisson.create(config);
    }

}
