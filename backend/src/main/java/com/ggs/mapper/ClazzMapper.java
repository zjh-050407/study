package com.ggs.mapper;

import com.ggs.pojo.Clazz;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ClazzMapper {
    List<Clazz> listClazz();
    Clazz getById(@Param("cid") Integer cid);
    int add(Clazz clazz);
    int update(Clazz clazz);
    int delete(@Param("cid") Integer cid);
    List<Clazz> listByDept(@Param("did") Integer did);

    int countByTeacher(@Param("tid") Integer tid);
}
