package com.ggs.service;

import com.ggs.mapper.StatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StatServiceImpl implements StatService {

    @Autowired
    private StatMapper statMapper;

    @Override
    public List<Map<String, Object>> countBySex() {
        return statMapper.countBySex();
    }

    @Override
    public List<Map<String, Object>> countByDept() {
        return statMapper.countByDept();
    }

    @Override
    public List<Map<String, Object>> countByAgeGroup() {
        return statMapper.countByAgeGroup();
    }

    @Override
    public List<Map<String, Object>> countByClazz() {
        return statMapper.countByClazz();
    }

    @Override
    public List<Map<String, Object>> avgByCourse() {
        return statMapper.avgByCourse();
    }

    @Override
    public List<Map<String, Object>> countByGrade() {
        return statMapper.countByGrade();
    }
}
