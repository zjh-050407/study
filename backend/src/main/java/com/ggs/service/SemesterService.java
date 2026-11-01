package com.ggs.service;

import com.ggs.pojo.Semester;

import java.util.List;

public interface SemesterService {

    List<Semester> listAll();

    void add(Semester semester);

    void update(Semester semester);

    void delete(int id);

    void setCurrent(int id);
}