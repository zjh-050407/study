package com.ggs.mapper;

import com.ggs.pojo.TeachingWeek;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeachingWeekMapper {

    List<TeachingWeek> list(@Param("semesterId") Integer semesterId);

    TeachingWeek getById(@Param("id") Integer id);

    int insert(TeachingWeek week);

    int update(TeachingWeek week);

    int delete(@Param("id") Integer id);
}
