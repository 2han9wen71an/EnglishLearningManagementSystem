package com.chun.myspringboot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Web安全配置类
 */
@Configuration
public class WebSecurityConfig {
    
    @Autowired
    private JwtConfig jwtConfig;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
