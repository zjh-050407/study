package com.ggs.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatsMapper {

    Map<String, Object> overview();

    List<Map<String, Object>> studentByDept();

    List<Map<String, Object>> scoreDistribution();

    List<Map<String, Object>> selectionHot();

    List<Map<String, Object>> evaluationAvg();

    Map<String, Object> examOverview();
}
