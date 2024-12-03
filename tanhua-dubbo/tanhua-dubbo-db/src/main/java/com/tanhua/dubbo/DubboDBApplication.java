package com.tanhua.dubbo;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan("com.tanhua.dubbo.mappers")
public class DubboDBApplication {
    public static void main(String[] args) {
        SpringApplication.run(DubboDBApplication.class, args);
    }

    // 添加分页插件配置
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor(); // MyBatis-Plus的分页插件
    }
}
