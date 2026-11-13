package com.ggs.controller;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.service.TeachTaskService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/teachTask")
public class TeachTaskController {

    @Autowired
    private TeachTaskService teachTaskService;

    @GetMapping("/list")
    public ResultVO list() {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(teachTaskService.listAll());
    }

    @GetMapping("/undone")
    public ResultVO listUndone() {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(teachTaskService.listUndone());
    }

    @OperationLogAnnotation("按培养方案生成教学任务")
    @PostMapping("/generate")
    public ResultVO generate() {
        RoleCheckUtil.requireRole("professor", "admin");
        int created = teachTaskService.generate();
        return ResultVO.success(Map.of("created", created));
    }

    @OperationLogAnnotation("分配授课教师")
    @PutMapping("/{id}/teacher")
    public ResultVO assignTeacher(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        RoleCheckUtil.requireRole("professor", "admin");
        Object tid = body.get("tid");
        teachTaskService.assignTeacher(id, tid == null ? null : Integer.valueOf(String.valueOf(tid)));
        return ResultVO.success();
    }

    @OperationLogAnnotation("推进教学任务状态")
    @PutMapping("/{id}/status")
    public ResultVO updateStatus(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        RoleCheckUtil.requireRole("professor", "admin");
        Object status = body.get("status");
        teachTaskService.updateStatus(id, status == null ? null : Integer.valueOf(String.valueOf(status)));
        return ResultVO.success();
    }

    @OperationLogAnnotation("删除教学任务")
    @DeleteMapping("/{id}")
    public ResultVO delete(@PathVariable Integer id) {
        RoleCheckUtil.requireRole("professor", "admin");
        teachTaskService.delete(id);
        return ResultVO.success();
    }
}
