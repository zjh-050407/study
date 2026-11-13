package com.ggs.mapper;

import com.ggs.pojo.TeachTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeachTaskMapper {
    List<TeachTask> listAll();
    TeachTask getById(@Param("id") Integer id);
    int countByCourseCidSem(@Param("courseId") Integer courseId, @Param("cid") Integer cid, @Param("semesterId") Integer semesterId);
    int insert(TeachTask teachTask);
    int updateTid(@Param("id") Integer id, @Param("tid") Integer tid);
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    int delete(@Param("id") Integer id);
    List<TeachTask> listUndone();
}
