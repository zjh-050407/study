package com.ggs.mapper;

import com.ggs.pojo.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudentMapper {

    List<Student> listStu();

    int delete(@Param("sid") Long sid);

    List<Student> getStuByAge(@Param("age") Integer age);

    int updateAge(@Param("sid") Long sid, @Param("age") Integer age);

    int addStu(Student student);

    int updateStu(Student student);

    List<Student> getStu(Student student);

    List<Student> listStuByPage(@Param("offset") int offset,
                                 @Param("size") int size,
                                 @Param("name") String name,
                                 @Param("cname") String cname);

    int countStu(@Param("name") String name, @Param("cname") String cname);

    int countByClazz(@Param("cid") Integer cid);
}