package com.ggs.util;

import com.ggs.exception.BusinessException;
import com.ggs.interceptor.JwtInterceptor;

import java.util.Arrays;

public class RoleCheckUtil {

    public static void requireRole(String... allowedRoles) {
        String currentRole = JwtInterceptor.ROLE_HOLDER.get();
        if (currentRole == null || !Arrays.asList(allowedRoles).contains(currentRole)) {
            throw new BusinessException("权限不足，无法访问该资源");
        }
    }
}