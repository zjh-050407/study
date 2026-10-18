
package com.ggs.service;

import com.ggs.pojo.Student;

import java.util.List;

public interface StudentService {

    List<Student> listStu();

    int delete(Long sid);

    List<Student> getStuByAge(int age);

    int updateAge(long sid, long age);

    int addStu(Student student);

    int updateStu(Student student);

    List<Student> getStu(Student student);

    int countStu(String name, String cname);
}