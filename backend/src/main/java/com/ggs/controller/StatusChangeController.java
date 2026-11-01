package com.ggs.controller;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.interceptor.JwtInterceptor;
import com.ggs.pojo.StatusChange;
import com.ggs.service.StatusChangeService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/statusChange")
public class StatusChangeController {

    @Autowired
    private StatusChangeService statusChangeService;

    @OperationLogAnnotation("学籍异动登记")
    @PostMapping
    public ResultVO register(@RequestBody StatusChange change) {
        RoleCheckUtil.requireRole("professor", "admin");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        statusChangeService.register(change, uid == null ? null : uid.intValue());
        return ResultVO.success();
    }

    @GetMapping("/list")
    public ResultVO listAll() {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(statusChangeService.listAll());
    }

    /** 指定学生的异动历史 */
    @GetMapping("/history")
    public ResultVO history(@RequestParam Integer sid) {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(statusChangeService.listBySid(sid));
    }

    /** 指定学生当前学籍状态（无记录=在校） */
    @GetMapping("/current")
    public ResultVO current(@RequestParam Integer sid) {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(statusChangeService.currentStatus(sid));
    }
}