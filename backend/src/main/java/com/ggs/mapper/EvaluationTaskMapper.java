package com.ggs.mapper;

import com.ggs.pojo.EvaluationTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EvaluationTaskMapper {

    int insert(EvaluationTask task);

    EvaluationTask getById(@Param("id") Integer id);

    List<EvaluationTask> listAll();

    /** 某学期进行中的评教任务（status=1），无则 null */
    EvaluationTask getActiveBySemester(@Param("semesterId") Integer semesterId);

    int countActiveBySemester(@Param("semesterId") Integer semesterId);

    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);
}