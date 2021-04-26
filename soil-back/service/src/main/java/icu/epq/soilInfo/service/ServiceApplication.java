package icu.epq.soilInfo.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * service 启动器
 *
 * @author EPQ
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan(basePackages = "icu.epq.soilInfo.service.mapper")
public class ServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }

}
