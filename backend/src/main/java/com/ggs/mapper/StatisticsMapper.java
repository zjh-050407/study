package com.ggs.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper {

    int countStudent();

    int countTeacher();

    int countCourse();

    int countClazz();

    List<Map<String, Object>> deptStudentCount();

    List<Map<String, Object>> genderDistribution();

    List<Map<String, Object>> ageDistribution();

    List<Map<String, Object>> avgScoreByCourse();

    List<Map<String, Object>> gradeDistribution();

    List<Map<String, Object>> scheduleHeatmap();

    List<Map<String, Object>> recentLogs();
}
