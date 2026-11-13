package com.ggs.mapper;

import com.ggs.pojo.Classroom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ClassroomMapper {
    List<Classroom> listByPage(@Param("offset") int offset, @Param("size") int size);
    int countAll();
    Classroom getById(@Param("id") Integer id);
    int countByName(@Param("name") String name, @Param("excludeId") Integer excludeId);
    int insert(Classroom classroom);
    int update(Classroom classroom);
    int delete(@Param("id") Integer id);
}
