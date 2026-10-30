package com.ggs.service;

import com.ggs.mapper.StatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private StatisticsMapper statisticsMapper;

    @Override
    public Map<String, Object> dashboard() {
        Map<String, Object> result = new HashMap<>();

        Map<String, Integer> kpi = new HashMap<>();
        kpi.put("studentCount", statisticsMapper.countStudent());
        kpi.put("teacherCount", statisticsMapper.countTeacher());
        kpi.put("courseCount", statisticsMapper.countCourse());
        kpi.put("clazzCount", statisticsMapper.countClazz());
        result.put("kpi", kpi);

        result.put("deptDistribution", statisticsMapper.deptStudentCount());
        result.put("genderDistribution", statisticsMapper.genderDistribution());
        result.put("ageDistribution", statisticsMapper.ageDistribution());
        result.put("avgScoreByCourse", statisticsMapper.avgScoreByCourse());
        result.put("gradeDistribution", statisticsMapper.gradeDistribution());
        result.put("scheduleHeatmap", statisticsMapper.scheduleHeatmap());
        result.put("recentLogs", statisticsMapper.recentLogs());

        return result;
    }
}
