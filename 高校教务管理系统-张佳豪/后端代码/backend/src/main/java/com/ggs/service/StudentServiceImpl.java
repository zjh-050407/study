package com.ggs.service;

import com.ggs.mapper.StudentMapper;
import com.ggs.pojo.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentMapper studentMapper;

    @Override
    public List<Student> listStu() {
        return studentMapper.listStu();
    }

    @Override
    public int delete(Long sid) {
        return studentMapper.delete(sid);
    }

    @Override
    public List<Student> getStuByAge(int age) {
        return studentMapper.getStuByAge(age);
    }

    @Override
    public int updateAge(long sid, long age) {
        return studentMapper.updateAge(sid, (int) age);
    }

    @Override
    public int addStu(Student student) {
        return studentMapper.addStu(student);
    }

    @Override
    public int updateStu(Student student) {
        return studentMapper.updateStu(student);
    }

    @Override
    public List<Student> getStu(Student student) {
        return studentMapper.getStu(student);
    }

    @Override
    public int countStu(String name, String cname) {
        return studentMapper.countStu(name, cname);
    }
}