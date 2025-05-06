package com.chun.myspringboot.config;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.util.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 获取请求路径
        String requestURI = request.getRequestURI();

        // 如果是登录、注册、激活等不需要验证的路径，直接放行
        if (isExcludedPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取请求头中的token
        String header = request.getHeader(jwtConfig.getHeader());

        // 如果请求头中没有token，返回401
        if (!StringUtils.hasText(header) || !header.startsWith(jwtConfig.getTokenPrefix() + " ")) {
            handleAuthenticationError(response, "未授权，请先登录");
            return;
        }

        try {
            // 从请求头中获取token
            String token = header.substring((jwtConfig.getTokenPrefix() + " ").length());

            // 从token中获取用户名
            String username = jwtTokenUtil.getUsernameFromToken(token);

            // 如果token无效，返回401
            if (username == null) {
                handleAuthenticationError(response, "无效的token");
                return;
            }

            // 将用户信息存入请求属性中，供后续使用
            request.setAttribute("username", username);
            request.setAttribute("userId", jwtTokenUtil.getClaimFromToken(token, claims -> claims.get("userId")));
            request.setAttribute("role", jwtTokenUtil.getClaimFromToken(token, claims -> claims.get("role")));

            // 继续执行过滤器链
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            handleAuthenticationError(response, "token验证失败：" + e.getMessage());
        }
    }

    /**
     * 处理认证错误
     */
    private void handleAuthenticationError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(message)));
    }

    /**
     * 判断是否是排除的路径
     */
    private boolean isExcludedPath(String requestURI) {
        // 登录、注册、激活等不需要验证的路径
        return requestURI.startsWith("/api/users/login") ||
                requestURI.startsWith("/api/users/admin/login") ||
                requestURI.startsWith("/api/users/register") ||
                requestURI.startsWith("/api/users/activate") ||
                // 音频文件路径，不需要身份认证
                requestURI.startsWith("/api/files/audio/") ||
                // Swagger相关路径
                requestURI.equals("/swagger-ui.html") ||
                requestURI.startsWith("/swagger-resources") ||
                requestURI.startsWith("/v2/api-docs") ||
                requestURI.startsWith("/webjars/");
    }
}
