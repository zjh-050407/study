package com.ggs.mapper;

import com.ggs.pojo.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentMapper {
    List<Department> listDepartment();
    Department getById(@Param("did") Integer did);
    int add(Department department);
    int update(Department department);
    int delete(@Param("did") Integer did);
}
