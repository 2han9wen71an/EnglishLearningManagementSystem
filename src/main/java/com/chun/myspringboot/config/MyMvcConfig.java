package com.chun.myspringboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyMvcConfig implements WebMvcConfigurer {
    // 前后端分离后不再需要视图控制器
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 不再配置视图映射，由前端路由处理
    }

    //配置静态资源处理器
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置Swagger UI资源处理器
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    //拦截所有，放行部分路径
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginHandlerInterceptor()).addPathPatterns("/**").excludePathPatterns("/index.html","/","/login","/css/**",
                "/js/**","/images/**","","/register.html","/register","/user/checkCode","/toAdmin","/admin","/lib/**","/static/**",
                "/api/**", // 排除所有API请求
                "/swagger-ui.html", // Swagger UI HTML
                "/swagger-resources/**", // Swagger资源
                "/v2/api-docs/**", // API文档
                "/webjars/**"); // Swagger UI依赖
    }

}
