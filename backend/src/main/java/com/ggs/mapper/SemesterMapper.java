package com.ggs.mapper;

import com.ggs.pojo.Semester;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SemesterMapper {

    List<Semester> listAll();

    Semester getById(@Param("id") int id);

    int countByName(@Param("name") String name, @Param("excludeId") Integer excludeId);

    int insert(Semester semester);

    int update(Semester semester);

    int delete(@Param("id") int id);

    int clearCurrent();

    int setCurrent(@Param("id") int id);
}