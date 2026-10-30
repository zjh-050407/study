package com.ggs.service;

import java.util.List;
import java.util.Map;

public interface ScoreWarningService {
    List<Map<String, Object>> failScores();

    List<Map<String, Object>> lowGpaStudents();
}
