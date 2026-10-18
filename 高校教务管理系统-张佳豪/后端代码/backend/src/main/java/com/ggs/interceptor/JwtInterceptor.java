package com.ggs.interceptor;

import com.ggs.mapper.AdminMapper;
import com.ggs.pojo.Admin;
import com.ggs.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private AdminMapper adminMapper;

    public static final ThreadLocal<Long> UID_HOLDER = new ThreadLocal<>();
    public static final ThreadLocal<String> USERNAME_HOLDER = new ThreadLocal<>();
    public static final ThreadLocal<String> ROLE_HOLDER = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || "/login".equals(path) || "/register".equals(path) || path.startsWith("/swagger") || path.startsWith("/v3")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":1,\"message\":\"未登录或Token无效\",\"data\":null}");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            if (JwtUtil.isExpired(token)) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":1,\"message\":\"Token已过期\",\"data\":null}");
                return false;
            }
            Claims claims = JwtUtil.parseToken(token);
            Long uid = Long.valueOf(claims.getSubject());
            UID_HOLDER.set(uid);
            USERNAME_HOLDER.set(claims.get("username", String.class));

            Admin admin = adminMapper.getById(uid);
            if (admin != null) {
                ROLE_HOLDER.set(admin.getRole());
            }
        } catch (Exception e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":1,\"message\":\"Token解析失败\",\"data\":null}");
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UID_HOLDER.remove();
        USERNAME_HOLDER.remove();
        ROLE_HOLDER.remove();
    }
}