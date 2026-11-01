package com.ggs.controller;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.interceptor.JwtInterceptor;
import com.ggs.service.GraduateAuditService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/graduate")
public class GraduateAuditController {

    @Autowired
    private GraduateAuditService auditService;

    @OperationLogAnnotation("毕业审核")
    @PostMapping("/audit")
    public ResultVO audit(@RequestBody Map<String, Object> params) {
        RoleCheckUtil.requireRole("professor", "admin");
        Integer sid = params.get("sid") == null ? null : Integer.valueOf(String.valueOf(params.get("sid")));
        Double requiredTotalCredit = params.get("requiredTotalCredit") == null ? null
                : Double.valueOf(String.valueOf(params.get("requiredTotalCredit")));
        Double requiredGpa = params.get("requiredGpa") == null ? null
                : Double.valueOf(String.valueOf(params.get("requiredGpa")));
        Long uid = JwtInterceptor.UID_HOLDER.get();
        return ResultVO.success(auditService.audit(sid, requiredTotalCredit, requiredGpa,
                uid == null ? null : uid.intValue()));
    }

    /** 审核历史：sid 为空查全部 */
    @GetMapping("/history")
    public ResultVO history(@RequestParam(required = false) Integer sid) {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(auditService.history(sid));
    }
}