package com.ggs.controller;

import com.ggs.interceptor.JwtInterceptor;
import com.ggs.service.PrintService;
import com.ggs.util.RoleCheckUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 证明打印（批次N）：成绩单 / 在读证明。
 * 学生可打印本人；管理员/教授可指定学号打印。
 */
@RestController
@RequestMapping("/print")
public class PrintController {

    @Autowired
    private PrintService printService;

    /** 解析目标学号：学生=本人；管理员/教授=可指定 */
    private Integer resolveSid(Integer sid) {
        String role = JwtInterceptor.ROLE_HOLDER.get();
        Long uid = JwtInterceptor.UID_HOLDER.get();
        if ("student".equals(role)) {
            return uid == null ? null : uid.intValue();
        }
        RoleCheckUtil.requireRole("professor", "admin");
        return sid;
    }

    @GetMapping("/transcript")
    public void transcript(@RequestParam(required = false) Integer sid, HttpServletResponse response) throws Exception {
        Integer target = resolveSid(sid);
        if (target == null) {
            response.setStatus(400);
            return;
        }
        printService.transcript(target, response);
    }

    @GetMapping("/enrollment")
    public void enrollment(@RequestParam(required = false) Integer sid, HttpServletResponse response) throws Exception {
        Integer target = resolveSid(sid);
        if (target == null) {
            response.setStatus(400);
            return;
        }
        printService.enrollment(target, response);
    }
}
