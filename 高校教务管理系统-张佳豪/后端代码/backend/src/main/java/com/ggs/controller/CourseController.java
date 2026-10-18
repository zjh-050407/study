package com.ggs.controller;

import com.ggs.pojo.Course;
import com.ggs.service.CourseService;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping("/list")
    public ResultVO listCourse() {
        List<Course> list = courseService.listCourse();
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
        courseService.delete(courseId);
        return ResultVO.success();
    }
}
