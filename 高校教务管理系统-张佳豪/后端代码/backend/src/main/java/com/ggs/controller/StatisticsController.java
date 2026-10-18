package com.ggs.controller;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.service.StatisticsService;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @OperationLogAnnotation("查看数据大屏")
    @GetMapping("/dashboard")
    public ResultVO dashboard() {
        Map<String, Object> data = statisticsService.dashboard();
        return ResultVO.success(data);
    }
}
