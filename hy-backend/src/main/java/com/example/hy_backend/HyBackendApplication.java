package com.example.hy_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@MapperScan("com.example.hy_backend.mapper") // 扫描Mapper包
@ComponentScan("com.example.hy_backend")
public class HyBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(HyBackendApplication.class, args);
    }
}
