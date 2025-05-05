package com.chun.myspringboot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置类
 */
@Configuration
public class JwtConfig {
    
    /**
     * JWT密钥
     */
    @Value("${jwt.secret:EnglishLearningSystemSecretKey}")
    private String secret;
    
    /**
     * Token有效期（秒）
     * 默认24小时
     */
    @Value("${jwt.expiration:86400}")
    private Long expiration;
    
    /**
     * Token前缀
     */
    @Value("${jwt.tokenPrefix:Bearer}")
    private String tokenPrefix;
    
    /**
     * 存放Token的Header Key
     */
    @Value("${jwt.header:Authorization}")
    private String header;
    
    public String getSecret() {
        return secret;
    }
    
    public Long getExpiration() {
        return expiration;
    }
    
    public String getTokenPrefix() {
        return tokenPrefix;
    }
    
    public String getHeader() {
        return header;
    }
}
