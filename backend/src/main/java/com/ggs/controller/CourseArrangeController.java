package com.ggs.controller;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.pojo.CourseArrange;
import com.ggs.service.CourseArrangeService;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courseArrange")
public class CourseArrangeController {

    @Autowired
    private CourseArrangeService courseArrangeService;

    @GetMapping("/list")
    public ResultVO listArrange() {
        List<CourseArrange> list = courseArrangeService.listArrange();
        return ResultVO.success(list);
    }

    @OperationLogAnnotation("新增排课")
    @PostMapping
    public ResultVO add(@RequestBody CourseArrange arrange) {
        String conflict = courseArrangeService.checkConflict(arrange);
        if (conflict != null) {
            return ResultVO.err(conflict);
        }
        int rows = courseArrangeService.add(arrange);
        if (rows > 0) {
            return ResultVO.success();
        }
        return ResultVO.err("新增失败");
    }

    @OperationLogAnnotation("修改排课")
    @PutMapping
    public ResultVO update(@RequestBody CourseArrange arrange) {
        String conflict = courseArrangeService.checkConflict(arrange);
        if (conflict != null) {
            return ResultVO.err(conflict);
        }
        int rows = courseArrangeService.update(arrange);
        if (rows > 0) {
            return ResultVO.success();
        }
        return ResultVO.err("更新失败");
    }

    @OperationLogAnnotation("删除排课")
    @DeleteMapping("/{arrangeId}")
    public ResultVO delete(@PathVariable Integer arrangeId) {
        int rows = courseArrangeService.delete(arrangeId);
        if (rows > 0) {
            return ResultVO.success();
        }
        return ResultVO.err("删除失败");
    }

    @GetMapping("/teacher/{tid}")
    public ResultVO listByTeacher(@PathVariable Integer tid) {
        List<CourseArrange> list = courseArrangeService.listByTeacher(tid);
        return ResultVO.success(list);
    }
}
