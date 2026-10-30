package com.ggs.controller;

import com.ggs.service.StatService;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stat")
public class StatController {

    @Autowired
    private StatService statService;

    @GetMapping("/sex")
    public ResultVO countBySex() {
        List<Map<String, Object>> data = statService.countBySex();
        return ResultVO.success(data);
    }

    @GetMapping("/dept")
    public ResultVO countByDept() {
        List<Map<String, Object>> data = statService.countByDept();
        return ResultVO.success(data);
    }

    @GetMapping("/age")
    public ResultVO countByAgeGroup() {
        List<Map<String, Object>> data = statService.countByAgeGroup();
        return ResultVO.success(data);
    }

    @GetMapping("/clazz")
    public ResultVO countByClazz() {
        List<Map<String, Object>> data = statService.countByClazz();
        return ResultVO.success(data);
    }

    @GetMapping("/course/avg")
    public ResultVO avgByCourse() {
        List<Map<String, Object>> data = statService.avgByCourse();
        return ResultVO.success(data);
    }

    @GetMapping("/grade")
    public ResultVO countByGrade() {
        List<Map<String, Object>> data = statService.countByGrade();
        return ResultVO.success(data);
    }
}
