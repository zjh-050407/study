package com.ggs.controller;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.pojo.Classroom;
import com.ggs.service.ClassroomService;
import com.ggs.util.PageUtil;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/classroom")
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    @GetMapping("/list")
    public ResultVO list(@RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size) {
        RoleCheckUtil.requireRole("professor", "admin");
        page = PageUtil.clampPage(page);
        size = PageUtil.clampSize(size);
        return ResultVO.success(classroomService.list(page, size));
    }

    @OperationLogAnnotation("新增教室")
    @PostMapping
    public ResultVO add(@RequestBody Classroom classroom) {
        RoleCheckUtil.requireRole("professor", "admin");
        classroomService.add(classroom);
        return ResultVO.success();
    }

    @OperationLogAnnotation("修改教室")
    @PutMapping
    public ResultVO update(@RequestBody Classroom classroom) {
        RoleCheckUtil.requireRole("professor", "admin");
        classroomService.update(classroom);
        return ResultVO.success();
    }

    @OperationLogAnnotation("删除教室")
    @DeleteMapping("/{id}")
    public ResultVO delete(@PathVariable Integer id) {
        RoleCheckUtil.requireRole("professor", "admin");
        classroomService.delete(id);
        return ResultVO.success();
    }
}
