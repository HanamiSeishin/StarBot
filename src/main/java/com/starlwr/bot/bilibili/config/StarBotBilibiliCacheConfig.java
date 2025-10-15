package com.starlwr.bot.bilibili.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.starlwr.bot.core.plugin.StarBotComponent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * StarBotBilibili 缓存配置类
 */
@Configuration
@StarBotComponent
public class StarBotBilibiliCacheConfig {
    @Resource
    private CaffeineCacheManager cacheManager;

    @PostConstruct
    public void init() {
        cacheManager.registerCustomCache("bilibiliApiCache",
                Caffeine.newBuilder()
                        .expireAfterAccess(5, TimeUnit.MINUTES)
                        .maximumSize(100000)
                        .build());

        cacheManager.registerCustomCache("bilibiliDynamicImageCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.MINUTES)
                        .maximumSize(10)
                        .build());
    }

    @Bean("cacheKeyGenerator")
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> method.getName() + ":" + Arrays.toString(params);
    }
}
