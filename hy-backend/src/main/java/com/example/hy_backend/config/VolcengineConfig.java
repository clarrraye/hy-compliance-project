package com.example.hy_backend.config;

import com.volcengine.ark.runtime.service.ArkService;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class VolcengineConfig {

    // 默认使用环境变量获取，如果在application.properties覆盖则使用配置
    // 为了容错，如果apiKey为空，可能需要提示用户
    @Value("${volcengine.ark.apiKey}")
    private String apiKey;

    @Value("${volcengine.ark.baseUrl:https://ark.cn-beijing.volces.com/api/v3}")
    private String baseUrl;

    @Bean(destroyMethod = "shutdownExecutor")
    public ArkService arkService() {
        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();
        return ArkService.builder()
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
    }
}
