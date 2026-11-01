package com.ggs.mapper;

import com.ggs.pojo.PlanCourse;
import com.ggs.pojo.TrainingPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TrainingPlanMapper {

    List<TrainingPlan> listPlans();

    TrainingPlan getById(@Param("id") Integer id);

    TrainingPlan getByCid(@Param("cid") Integer cid);

    /** 班级维度唯一：查指定班级的方案（可排除自身） */
    int countByCid(@Param("cid") Integer cid, @Param("excludeId") Integer excludeId);

    int insert(TrainingPlan plan);

    int update(TrainingPlan plan);

    int delete(@Param("id") Integer id);

    /* ==================== 方案课程 ==================== */

    List<PlanCourse> listCourses(@Param("planId") Integer planId);

    int countCourse(@Param("planId") Integer planId, @Param("courseId") Integer courseId, @Param("excludeId") Integer excludeId);

    int insertCourse(PlanCourse course);

    int updateCourse(PlanCourse course);

    int deleteCourse(@Param("id") Integer id);

    int deleteCoursesByPlan(@Param("planId") Integer planId);
}