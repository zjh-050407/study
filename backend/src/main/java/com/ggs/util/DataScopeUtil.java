package com.ggs.util;

import com.ggs.exception.BusinessException;
import com.ggs.interceptor.JwtInterceptor;

public class DataScopeUtil {

    /** 当前用户数据范围：null=全局（admin/未归属院系的账号），非null=仅该院系 */
    public static Integer currentDid() {
        return JwtInterceptor.DID_HOLDER.get();
    }

    /** 目标数据是否在本人数据范围内（did=null 表示全局） */
    public static boolean inScope(Integer did) {
        Integer mine = currentDid();
        return mine == null || mine.equals(did);
    }

    /** 写操作数据范围校验：越界直接抛异常 */
    public static void requireInScope(Integer did) {
        Integer mine = currentDid();
        if (mine != null && !mine.equals(did)) {
            throw new BusinessException("权限不足，仅可操作本院系数据");
        }
    }
}
