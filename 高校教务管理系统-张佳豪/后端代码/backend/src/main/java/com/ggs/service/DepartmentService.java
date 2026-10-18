package com.ggs.service;

import com.ggs.pojo.Department;

import java.util.List;

public interface DepartmentService {
    List<Department> listDepartment();
    Department getById(Integer did);
    int add(Department department);
    int update(Department department);
    int delete(Integer did);
}
