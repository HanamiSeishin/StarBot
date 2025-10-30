package com.starlwr.bot.bilibili.config;

import com.starlwr.bot.core.plugin.StarBotComponent;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * StarBotBilibili 缓存 Key 配置类
 */
@Configuration
@StarBotComponent
public class StarBotBilibiliCacheKeyConfig {
    @Bean("cacheKeyGenerator")
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            String paramsPart = Arrays.stream(params)
                    .map(param -> {
                        if (param == null) return "null";

                        try {
                            Field idField = param.getClass().getDeclaredField("id");
                            idField.setAccessible(true);
                            return idField.get(param).toString();
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            return param.toString();
                        }
                    })
                    .collect(Collectors.joining(","));

            return method.getName() + ":[" + paramsPart + "]";
        };
    }
}
