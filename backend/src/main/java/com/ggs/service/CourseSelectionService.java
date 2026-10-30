package com.ggs.service;

import com.ggs.pojo.CourseSelection;

import java.util.List;

public interface CourseSelectionService {
    List<CourseSelection> listAll();

    List<CourseSelection> listByStudent(Integer sid);

    void add(Integer sid, Integer courseId);

    void delete(Integer id);
}