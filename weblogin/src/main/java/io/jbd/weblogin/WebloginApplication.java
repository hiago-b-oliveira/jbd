package io.jbd.weblogin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableCaching
@EnableDiscoveryClient
@EnableWebSecurity

public class WebloginApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebloginApplication.class, args);
    }
}
