package com.ggs.controller;

import com.ggs.mapper.CourseMapper;
import com.ggs.pojo.Course;
import com.ggs.service.CourseService;
import com.ggs.util.DataScopeUtil;
import com.ggs.util.PageUtil;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseMapper courseMapper;

    @GetMapping("/list")
    public ResultVO listCourse(@RequestParam(required = false) Integer page,
                               @RequestParam(required = false) Integer size) {
        Integer did = DataScopeUtil.currentDid();
        if (page != null && size != null) {
            page = PageUtil.clampPage(page);
            size = PageUtil.clampSize(size);
            int offset = (page - 1) * size;
            List<Course> list = courseMapper.listByPage(offset, size, did);
            int total = courseMapper.countAll(did);
            return ResultVO.success(new ResultVO.PageResult<>(list, total, page, size));
        }
        List<Course> list = courseMapper.listCourse(did);
        return ResultVO.success(list);
    }

    @GetMapping("/{courseId}")
    public ResultVO getById(@PathVariable Integer courseId) {
        Course course = courseService.getById(courseId);
        if (course == null) {
            return ResultVO.err("课程不存在");
        }
        return ResultVO.success(course);
    }

    @PostMapping
    public ResultVO addCourse(@RequestBody Course course) {
        courseService.add(course);
        return ResultVO.success();
    }

    @PutMapping
    public ResultVO updateCourse(@RequestBody Course course) {
        courseService.update(course);
        return ResultVO.success();
    }

    @DeleteMapping("/{courseId}")
    public ResultVO deleteCourse(@PathVariable Integer courseId) {
        RoleCheckUtil.requireRole("professor", "admin");
        courseService.delete(courseId);
        return ResultVO.success();
    }
}
