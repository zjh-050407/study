package com.ggs.aspect;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.interceptor.JwtInterceptor;
import com.ggs.mapper.OperationLogMapper;
import com.ggs.pojo.OperationLog;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Pointcut("@annotation(com.ggs.annotation.OperationLogAnnotation)")
    public void logPointCut() {}

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object result = point.proceed();

        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            OperationLogAnnotation annotation = signature.getMethod().getAnnotation(OperationLogAnnotation.class);

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return result;
            }
            HttpServletRequest request = attributes.getRequest();

            String username = JwtInterceptor.USERNAME_HOLDER.get();
            if (username == null) {
                username = "anonymous";
            }

            String params = Arrays.toString(point.getArgs());
            if (params.length() > 500) {
                params = params.substring(0, 500) + "...";
            }

            OperationLog log = new OperationLog();
            log.setUsername(username);
            log.setOperation(annotation.value());
            log.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());
            log.setParams(params);
            log.setIp(getIpAddr(request));
            log.setCreateTime(LocalDateTime.now());
            operationLogMapper.insert(log);
        } catch (Exception e) {
            // 日志记录不应影响主业务
            e.printStackTrace();
        }

        return result;
    }

    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
