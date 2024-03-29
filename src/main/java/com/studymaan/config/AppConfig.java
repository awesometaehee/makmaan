package com.studymaan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // 스프링 순환 참조가 발생되니 SecurityConfig 에서 외부로 분리
    @Bean
    public SecurityContextHolderStrategy securityContextHolderStrategy() {
        return SecurityContextHolder.getContextHolderStrategy();
    }

    // 스프링 순환 참조가 발생되니 SecurityConfig 에서 외부로 분리
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}
