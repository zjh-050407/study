package com.ggs.controller;

import com.alibaba.excel.EasyExcel;
import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.dto.ScoreExportDTO;
import com.ggs.pojo.Score;
import com.ggs.service.ScoreService;
import com.ggs.vo.ResultVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/score")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    @GetMapping("/list")
    public ResultVO listScore() {
        List<Score> list = scoreService.listScore();
        return ResultVO.success(list);
    }

    @GetMapping("/student/{sid}")
    public ResultVO listByStudent(@PathVariable Integer sid) {
        List<Score> list = scoreService.listByStudent(sid);
        return ResultVO.success(list);
    }

    @GetMapping("/course/{courseId}")
    public ResultVO listByCourse(@PathVariable Integer courseId) {
        List<Score> list = scoreService.listByCourse(courseId);
        return ResultVO.success(list);
    }

    @OperationLogAnnotation("录入成绩")
    @PostMapping
    public ResultVO addScore(@RequestBody Score score) {
        scoreService.add(score);
        return ResultVO.success();
    }

    @OperationLogAnnotation("修改成绩")
    @PutMapping
    public ResultVO updateScore(@RequestBody Score score) {
        scoreService.update(score);
        return ResultVO.success();
    }

    @OperationLogAnnotation("删除成绩")
    @DeleteMapping("/{scoreId}")
    public ResultVO deleteScore(@PathVariable Integer scoreId) {
        scoreService.delete(scoreId);
        return ResultVO.success();
    }

    @OperationLogAnnotation("导出成绩信息")
    @GetMapping("/export")
    public void exportScore(HttpServletResponse response) throws IOException {
        List<Score> list = scoreService.listScore();
        List<ScoreExportDTO> dtoList = list.stream().map(s -> {
            ScoreExportDTO dto = new ScoreExportDTO();
            BeanUtils.copyProperties(s, dto);
            return dto;
        }).collect(Collectors.toList());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("成绩信息", StandardCharsets.UTF_8).replace("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), ScoreExportDTO.class)
                .sheet("成绩信息")
                .doWrite(dtoList);
    }
}
