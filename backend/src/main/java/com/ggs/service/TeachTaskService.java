package com.ggs.service;

import com.ggs.pojo.TeachTask;

import java.util.List;

public interface TeachTaskService {
    int generate();
    List<TeachTask> listAll();
    List<TeachTask> listUndone();
    void assignTeacher(Integer id, Integer tid);
    void updateStatus(Integer id, Integer status);
    void delete(Integer id);
}
