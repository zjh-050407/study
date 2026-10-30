package com.ggs.controller;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.service.ScoreWarningService;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scoreWarning")
public class ScoreWarningController {

    @Autowired
    private ScoreWarningService scoreWarningService;

    @OperationLogAnnotation("查看成绩预警")
    @GetMapping("/list")
    public ResultVO list() {
        Map<String, Object> result = new HashMap<>();
        result.put("failScores", scoreWarningService.failScores());
        result.put("lowGpaStudents", scoreWarningService.lowGpaStudents());
        return ResultVO.success(result);
    }
}
