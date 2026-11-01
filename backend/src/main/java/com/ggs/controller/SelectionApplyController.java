package com.ggs.controller;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.interceptor.JwtInterceptor;
import com.ggs.pojo.SelectionApply;
import com.ggs.service.SelectionApplyService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/selectionApply")
public class SelectionApplyController {

    @Autowired
    private SelectionApplyService applyService;

    @OperationLogAnnotation("学生提交退选申请")
    @PostMapping
    public ResultVO apply(@RequestBody SelectionApply apply) {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        applyService.apply(sid, apply.getCourseId(), apply.getReason());
        return ResultVO.success();
    }

    @GetMapping("/my")
    public ResultVO my() {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        return ResultVO.success(applyService.listByStudent(sid));
    }

    @GetMapping("/pending")
    public ResultVO pending() {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(applyService.listPending());
    }

    @OperationLogAnnotation("通过退选申请")
    @PutMapping("/approve/{id}")
    public ResultVO approve(@PathVariable Integer id) {
        RoleCheckUtil.requireRole("professor", "admin");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        applyService.approve(id, uid == null ? null : uid.intValue());
        return ResultVO.success();
    }

    @OperationLogAnnotation("拒绝退选申请")
    @PutMapping("/reject/{id}")
    public ResultVO reject(@PathVariable Integer id) {
        RoleCheckUtil.requireRole("professor", "admin");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        applyService.reject(id, uid == null ? null : uid.intValue());
        return ResultVO.success();
    }
}