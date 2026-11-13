package com.ggs.interceptor;

import com.ggs.mapper.AdminMapper;
import com.ggs.mapper.ClazzMapper;
import com.ggs.mapper.StudentMapper;
import com.ggs.mapper.TeacherMapper;
import com.ggs.pojo.Admin;
import com.ggs.pojo.Clazz;
import com.ggs.pojo.Student;
import com.ggs.pojo.Teacher;
import com.ggs.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtInterceptor.class);

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ClazzMapper clazzMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public static final ThreadLocal<Long> UID_HOLDER = new ThreadLocal<>();
    public static final ThreadLocal<String> USERNAME_HOLDER = new ThreadLocal<>();
    public static final ThreadLocal<String> ROLE_HOLDER = new ThreadLocal<>();
    /** 数据范围：null=全局（admin/未归属），非null=仅该院系（student 按班级院系，professor 按教师归属） */
    public static final ThreadLocal<Integer> DID_HOLDER = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || "/login".equals(path) || "/register".equals(path)
                || "/captcha".equals(path) || path.startsWith("/swagger") || path.startsWith("/v3")) {
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
            // 安全加固：登出黑名单校验（Redis 故障时放行并告警，优先可用性）
            try {
                if (Boolean.TRUE.equals(redisTemplate.hasKey(JwtUtil.blacklistKey(token)))) {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":1,\"message\":\"Token已注销，请重新登录\",\"data\":null}");
                    return false;
                }
            } catch (Exception e) {
                log.warn("JWT 黑名单校验失败（Redis 不可用），本次放行: {}", e.getMessage());
            }
            Claims claims = JwtUtil.parseToken(token);
            Long uid = Long.valueOf(claims.getSubject());
            UID_HOLDER.set(uid);
            USERNAME_HOLDER.set(claims.get("username", String.class));

            // 优先使用 token 中的 role claim（学生等非 admin 表账号）；
            // 旧版 token 无 role claim，保持从 admin 表回退查询
            String role = claims.get("role", String.class);
            if (role != null) {
                ROLE_HOLDER.set(role);
            } else {
                Admin admin = adminMapper.getById(uid);
                if (admin != null) {
                    ROLE_HOLDER.set(admin.getRole());
                }
            }
            DID_HOLDER.set(resolveDid(ROLE_HOLDER.get(), USERNAME_HOLDER.get(), uid));
        } catch (Exception e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":1,\"message\":\"Token解析失败\",\"data\":null}");
            return false;
        }
        return true;
    }

    /** 解析数据范围：student→班级院系；professor→教师归属院系（admin.username 匹配 teacher.username）；admin→全局 */
    private Integer resolveDid(String role, String username, Long uid) {
        try {
            if ("student".equals(role)) {
                Student student = studentMapper.getById(uid.intValue());
                if (student != null && student.getCid() != null) {
                    Clazz clazz = clazzMapper.getById(student.getCid());
                    return clazz == null ? null : clazz.getDid();
                }
            } else if ("professor".equals(role) && username != null) {
                Teacher teacher = teacherMapper.getByUsername(username);
                if (teacher != null) {
                    return teacher.getDid();
                }
            }
        } catch (Exception ignored) {
            // 数据范围解析失败不阻断请求，按全局处理
        }
        return null;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UID_HOLDER.remove();
        USERNAME_HOLDER.remove();
        ROLE_HOLDER.remove();
        DID_HOLDER.remove();
    }
}