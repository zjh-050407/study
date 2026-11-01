package com.ggs.service;

import com.ggs.pojo.PlanCourse;
import com.ggs.pojo.TrainingPlan;

import java.util.List;

public interface TrainingPlanService {

    List<TrainingPlan> listPlans();

    void createPlan(TrainingPlan plan);

    void updatePlan(TrainingPlan plan);

    void deletePlan(Integer id);

    List<PlanCourse> listCourses(Integer planId);

    void addCourse(PlanCourse course);

    void updateCourse(PlanCourse course);

    void deleteCourse(Integer id);
}