package com.ggs.controller;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.pojo.Semester;
import com.ggs.service.SemesterService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/semester")
public class SemesterController {

    @Autowired
    private SemesterService semesterService;

    @GetMapping("/list")
    public ResultVO list() {
        List<Semester> list = semesterService.listAll();
        return ResultVO.success(list);
    }

    @OperationLogAnnotation("新增学期")
    @PostMapping
    public ResultVO add(@RequestBody Semester semester) {
        RoleCheckUtil.requireRole("professor", "admin");
        semesterService.add(semester);
        return ResultVO.success();
    }

    @OperationLogAnnotation("编辑学期")
    @PutMapping
    public ResultVO update(@RequestBody Semester semester) {
        RoleCheckUtil.requireRole("professor", "admin");
        semesterService.update(semester);
        return ResultVO.success();
    }

    @OperationLogAnnotation("删除学期")
    @DeleteMapping("/{id}")
    public ResultVO delete(@PathVariable int id) {
        RoleCheckUtil.requireRole("professor", "admin");
        semesterService.delete(id);
        return ResultVO.success();
    }

    @OperationLogAnnotation("设为当前学期")
    @PutMapping("/current/{id}")
    public ResultVO setCurrent(@PathVariable int id) {
        RoleCheckUtil.requireRole("professor", "admin");
        semesterService.setCurrent(id);
        return ResultVO.success();
    }
}