package com.ggs.controller;

import com.ggs.mapper.StatsMapper;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private StatsMapper statsMapper;

    @GetMapping("/overview")
    public ResultVO overview() {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(statsMapper.overview());
    }

    @GetMapping("/studentByDept")
    public ResultVO studentByDept() {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(statsMapper.studentByDept());
    }

    @GetMapping("/scoreDistribution")
    public ResultVO scoreDistribution() {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(statsMapper.scoreDistribution());
    }

    @GetMapping("/selectionHot")
    public ResultVO selectionHot() {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(statsMapper.selectionHot());
    }

    @GetMapping("/evaluationAvg")
    public ResultVO evaluationAvg() {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(statsMapper.evaluationAvg());
    }

    @GetMapping("/examOverview")
    public ResultVO examOverview() {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(statsMapper.examOverview());
    }
}
