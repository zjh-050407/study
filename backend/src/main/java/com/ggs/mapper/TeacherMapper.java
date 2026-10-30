package com.ggs.mapper;

import com.ggs.pojo.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeacherMapper {

    List<Teacher> listTeacher();

    Teacher getTeacherById(@Param("tid") Long tid);

    int addTeacher(Teacher teacher);

    int updateTeacher(Teacher teacher);

    int deleteTeacher(@Param("tid") Long tid);
}