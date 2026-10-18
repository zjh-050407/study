package com.ggs.service;

import com.ggs.mapper.ClazzMapper;
import com.ggs.mapper.DepartmentMapper;
import com.ggs.pojo.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private ClazzMapper clazzMapper;

    @Override
    public List<Department> listDepartment() {
        return departmentMapper.listDepartment();
    }

    @Override
    public Department getById(Integer did) {
        return departmentMapper.getById(did);
    }

    @Override
    public int add(Department department) {
        return departmentMapper.add(department);
    }

    @Override
    public int update(Department department) {
        return departmentMapper.update(department);
    }

    @Override
    public int delete(Integer did) {
        if (!clazzMapper.listByDept(did).isEmpty()) {
            throw new RuntimeException("该院系下存在班级，无法删除");
        }
        return departmentMapper.delete(did);
    }
}
