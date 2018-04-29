package io.jbd.weblogin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WebloginApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebloginApplication.class, args);
    }
}
