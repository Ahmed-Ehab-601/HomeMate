package com.homemate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class TestMailConfig {
    @Bean
    public JavaMailSender javaMailSender() {
        // Lightweight stub mail sender to satisfy bean requirements in tests
        return new JavaMailSenderImpl();
    }
}
