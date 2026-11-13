package com.ggs.mapper;

import com.ggs.pojo.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseMapper {
    List<Course> listCourse(@Param("did") Integer did);
    List<Course> listByPage(@Param("offset") int offset, @Param("size") int size, @Param("did") Integer did);
    int countAll(@Param("did") Integer did);
    Course getById(@Param("courseId") Integer courseId);
    int add(Course course);
    int update(Course course);
    int delete(@Param("courseId") Integer courseId);
}
