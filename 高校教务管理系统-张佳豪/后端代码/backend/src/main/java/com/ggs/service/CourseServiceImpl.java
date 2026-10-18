package com.ggs.service;

import com.ggs.mapper.CourseArrangeMapper;
import com.ggs.mapper.CourseMapper;
import com.ggs.mapper.ScoreMapper;
import com.ggs.pojo.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CourseArrangeMapper courseArrangeMapper;

    @Autowired
    private ScoreMapper scoreMapper;

    @Override
    public List<Course> listCourse() {
        return courseMapper.listCourse();
    }

    @Override
    public Course getById(Integer courseId) {
        return courseMapper.getById(courseId);
    }

    @Override
    public int add(Course course) {
        return courseMapper.add(course);
    }

    @Override
    public int update(Course course) {
        return courseMapper.update(course);
    }

    @Override
    public int delete(Integer courseId) {
        if (courseArrangeMapper.countByCourse(courseId) > 0) {
            throw new RuntimeException("该课程存在排课记录，无法删除");
        }
        if (!scoreMapper.listByCourse(courseId).isEmpty()) {
            throw new RuntimeException("该课程存在成绩记录，无法删除");
        }
        return courseMapper.delete(courseId);
    }
}
