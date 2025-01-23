package com.poelov.authrequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AuthRequestApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthRequestApplication.class, args);
    }
}
