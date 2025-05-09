package com.chun.myspringboot.config;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginHandlerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求的URI
        String requestURI = request.getRequestURI();

        // 判断是否是静态资源请求
        if (requestURI.startsWith("/static/")) {
            // 如果是静态资源，直接放行
            return true;
        }

        // 前后端分离架构下，使用JWT进行认证，不再使用Session
        // 此拦截器仅用于保护非API资源，API资源由JwtAuthenticationFilter保护
        // 由于所有页面请求都已经在MyMvcConfig中排除，此处直接放行
        return true;
    }
}
