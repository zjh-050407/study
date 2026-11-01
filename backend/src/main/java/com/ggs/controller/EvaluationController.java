package com.ggs.controller;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.interceptor.JwtInterceptor;
import com.ggs.pojo.EvaluationRecord;
import com.ggs.pojo.EvaluationTask;
import com.ggs.service.EvaluationService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    /* ==================== 教师/管理员：任务管理 ==================== */

    @OperationLogAnnotation("发起评教任务")
    @PostMapping("/task")
    public ResultVO createTask(@RequestBody EvaluationTask task) {
        RoleCheckUtil.requireRole("professor", "admin");
        evaluationService.createTask(task);
        return ResultVO.success();
    }

    @OperationLogAnnotation("关闭评教任务")
    @PutMapping("/task/{id}/close")
    public ResultVO closeTask(@PathVariable Integer id) {
        RoleCheckUtil.requireRole("professor", "admin");
        evaluationService.closeTask(id);
        return ResultVO.success();
    }

    @GetMapping("/task/list")
    public ResultVO listTasks() {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(evaluationService.listTasks());
    }

    /* ==================== 学生端：评教窗口与提交 ==================== */

    /** 当前学期评教窗口状态（活动任务/是否开启） */
    @GetMapping("/active")
    public ResultVO active() {
        RoleCheckUtil.requireRole("student");
        return ResultVO.success(evaluationService.activeWindow());
    }

    /** 本学期可评课程列表（含已评标记） */
    @GetMapping("/my/courses")
    public ResultVO myCourses() {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        return ResultVO.success(evaluationService.listEligibleForStudent(sid));
    }

    @OperationLogAnnotation("学生提交评教")
    @PostMapping("/submit")
    public ResultVO submit(@RequestBody EvaluationRecord record) {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        evaluationService.submit(sid, record);
        return ResultVO.success();
    }

    /* ==================== 教师/管理员：统计与导出 ==================== */

    @GetMapping("/stats")
    public ResultVO stats(@RequestParam Integer taskId) {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(evaluationService.stats(taskId));
    }

    @OperationLogAnnotation("导出评教统计")
    @GetMapping("/export")
    public ResultVO export(@RequestParam Integer taskId) {
        RoleCheckUtil.requireRole("admin");
        return ResultVO.success(evaluationService.exportCsv(taskId));
    }
}