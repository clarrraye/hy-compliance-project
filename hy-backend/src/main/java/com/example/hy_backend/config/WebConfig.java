package com.example.hy_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置类 - 注册未登录拦截器，配置拦截/放行路径
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                // 拦截所有需要登录的接口（功能1+后续所有模块）
                .addPathPatterns("/compliance/**")
                // 放行登录/退出接口+静态资源（无需登录即可访问）
                .excludePathPatterns(
                        "/user/login",
                        "/user/logout",
                        "/static/**",
                        "/favicon.ico"
                );
    }
}