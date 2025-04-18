package com.ddhouse.chat;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing // 자동 날짜 생성
public class DdhouseChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(DdhouseChatApplication.class, args);
    }

}
