package icu.epq.soilInfo.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * auth 启动类
 *
 * @author EPQ
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan(basePackages = "icu.epq.soilInfo.auth.mapper")
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
