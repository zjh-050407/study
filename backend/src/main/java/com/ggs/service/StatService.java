package com.ggs.service;

import java.util.List;
import java.util.Map;

public interface StatService {

    List<Map<String, Object>> countBySex();

    List<Map<String, Object>> countByDept();

    List<Map<String, Object>> countByAgeGroup();

    List<Map<String, Object>> countByClazz();

    List<Map<String, Object>> avgByCourse();

    List<Map<String, Object>> countByGrade();
}
