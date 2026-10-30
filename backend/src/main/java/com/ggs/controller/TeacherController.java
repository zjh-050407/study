package com.ggs.controller;

import com.ggs.pojo.Teacher;
import com.ggs.service.TeacherService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @GetMapping("/list")
    public ResultVO listTeacher() {
        RoleCheckUtil.requireRole("professor", "admin");
        List<Teacher> list = teacherService.listTeacher();
        return ResultVO.success(list);
    }

    @GetMapping("/{tid}")
    public ResultVO getById(@PathVariable Long tid) {
        RoleCheckUtil.requireRole("professor", "admin");
        Teacher teacher = teacherService.getTeacherById(tid);
        if (teacher == null) {
            return ResultVO.err("教师不存在");
        }
        return ResultVO.success(teacher);
    }

    @PostMapping
    public ResultVO addTeacher(@RequestBody Teacher teacher) {
        RoleCheckUtil.requireRole("professor", "admin");
        teacherService.addTeacher(teacher);
        return ResultVO.success();
    }

    @PutMapping
    public ResultVO updateTeacher(@RequestBody Teacher teacher) {
        RoleCheckUtil.requireRole("professor", "admin");
        teacherService.updateTeacher(teacher);
        return ResultVO.success();
    }

    @DeleteMapping("/{tid}")
    public ResultVO deleteTeacher(@PathVariable Long tid) {
        RoleCheckUtil.requireRole("professor", "admin");
        teacherService.deleteTeacher(tid);
        return ResultVO.success();
    }
}