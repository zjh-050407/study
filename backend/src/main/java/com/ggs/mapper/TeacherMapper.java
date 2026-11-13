package com.ggs.mapper;

import com.ggs.pojo.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeacherMapper {

    List<Teacher> listTeacher(@Param("did") Integer did);

    List<Teacher> listByPage(@Param("offset") int offset, @Param("size") int size, @Param("did") Integer did);

    int countAll(@Param("did") Integer did);

    Teacher getTeacherById(@Param("tid") Long tid);

    Teacher getByUsername(@Param("username") String username);

    int addTeacher(Teacher teacher);

    int updateTeacher(Teacher teacher);

    int deleteTeacher(@Param("tid") Long tid);
}