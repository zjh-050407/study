package com.ggs.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatMapper {

    List<Map<String, Object>> countBySex();

    List<Map<String, Object>> countByDept();

    List<Map<String, Object>> countByAgeGroup();

    List<Map<String, Object>> countByClazz();

    List<Map<String, Object>> avgByCourse();

    List<Map<String, Object>> countByGrade();
}
