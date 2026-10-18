package com.ggs.service;

import com.ggs.pojo.Course;

import java.util.List;

public interface CourseService {
    List<Course> listCourse();
    Course getById(Integer courseId);
    int add(Course course);
    int update(Course course);
    int delete(Integer courseId);
}
