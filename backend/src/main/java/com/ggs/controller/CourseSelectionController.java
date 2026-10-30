package com.ggs.controller;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.pojo.CourseSelection;
import com.ggs.service.CourseSelectionService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/selection")
public class CourseSelectionController {

    @Autowired
    private CourseSelectionService selectionService;

    @GetMapping("/list")
    public ResultVO list() {
        return ResultVO.success(selectionService.listAll());
    }

    @GetMapping("/listByStudent")
    public ResultVO listByStudent(@RequestParam Integer sid) {
        return ResultVO.success(selectionService.listByStudent(sid));
    }

    @OperationLogAnnotation("代学生选课")
    @PostMapping
    public ResultVO add(@RequestBody CourseSelection selection) {
        selectionService.add(selection.getSid(), selection.getCourseId());
        return ResultVO.success();
    }

    @OperationLogAnnotation("删除选课记录")
    @DeleteMapping("/{id}")
    public ResultVO delete(@PathVariable Integer id) {
        RoleCheckUtil.requireRole("professor", "admin");
        selectionService.delete(id);
        return ResultVO.success();
    }
}