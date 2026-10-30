package com.ggs.service;

import com.ggs.pojo.Teacher;

import java.util.List;

public interface TeacherService {

    List<Teacher> listTeacher();

    Teacher getTeacherById(Long tid);

    int addTeacher(Teacher teacher);

    int updateTeacher(Teacher teacher);

    int deleteTeacher(Long tid);
}