package com.starlwr.bot.bilibili.config;

import com.starlwr.bot.core.enums.LivePlatform;
import com.starlwr.bot.core.plugin.StarBotComponent;
import com.starlwr.bot.core.service.RedisService;
import com.starlwr.bot.core.util.RedisUtil;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * StarBotBilibili Redis 配置类
 */
@Profile("!core")
@Configuration
@StarBotComponent
public class StarBotBilibiliRedisConfig {
    @Resource
    private RedisService redisService;

    @Bean
    public RedisUtil bilibiliRedis() {
        return redisService.getRedis(LivePlatform.BILIBILI.getName());
    }
}
