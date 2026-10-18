package com.ggs.service;

import com.ggs.mapper.ClazzMapper;
import com.ggs.mapper.CourseArrangeMapper;
import com.ggs.mapper.TeacherMapper;
import com.ggs.pojo.Teacher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private ClazzMapper clazzMapper;

    @Autowired
    private CourseArrangeMapper courseArrangeMapper;

    @Override
    public List<Teacher> listTeacher() {
        return teacherMapper.listTeacher();
    }

    @Override
    public Teacher getTeacherById(Long tid) {
        return teacherMapper.getTeacherById(tid);
    }

    @Override
    public int addTeacher(Teacher teacher) {
        return teacherMapper.addTeacher(teacher);
    }

    @Override
    public int updateTeacher(Teacher teacher) {
        return teacherMapper.updateTeacher(teacher);
    }

    @Override
    public int deleteTeacher(Long tid) {
        if (clazzMapper.countByTeacher(tid.intValue()) > 0) {
            throw new RuntimeException("该教师是某班级的班主任，无法删除");
        }
        if (!courseArrangeMapper.listByTeacher(tid.intValue()).isEmpty()) {
            throw new RuntimeException("该教师存在排课记录，无法删除");
        }
        return teacherMapper.deleteTeacher(tid);
    }
}