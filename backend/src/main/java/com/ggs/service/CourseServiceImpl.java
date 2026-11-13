package com.ggs.service;

import com.ggs.mapper.CourseArrangeMapper;
import com.ggs.mapper.CourseMapper;
import com.ggs.mapper.ScoreMapper;
import com.ggs.pojo.Course;
import com.ggs.util.DataScopeUtil;
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
        return courseMapper.listCourse(DataScopeUtil.currentDid());
    }

    @Override
    public Course getById(Integer courseId) {
        return courseMapper.getById(courseId);
    }

    @Override
    public int add(Course course) {
        normalizeCapacity(course);
        return courseMapper.add(course);
    }

    @Override
    public int update(Course course) {
        normalizeCapacity(course);
        return courseMapper.update(course);
    }

    /** 容量默认 50；非法值回退默认（选课容量控制阶段会再严格校验） */
    private void normalizeCapacity(Course course) {
        if (course.getCapacity() == null || course.getCapacity() < 1) {
            course.setCapacity(50);
        }
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
