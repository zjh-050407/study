package com.ggs.controller;

import com.alibaba.excel.EasyExcel;
import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.dto.ScoreExportDTO;
import com.ggs.interceptor.JwtInterceptor;
import com.ggs.mapper.ScoreMapper;
import com.ggs.pojo.Score;
import com.ggs.service.ScoreService;
import com.ggs.util.DataScopeUtil;
import com.ggs.util.PageUtil;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/score")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private ScoreMapper scoreMapper;

    @GetMapping("/list")
    public ResultVO listScore(@RequestParam(required = false) Integer page,
                              @RequestParam(required = false) Integer size,
                              @RequestParam(required = false) Integer status) {
        Integer did = DataScopeUtil.currentDid();
        if (page != null && size != null) {
            page = PageUtil.clampPage(page);
            size = PageUtil.clampSize(size);
            int offset = (page - 1) * size;
            List<Score> list = scoreMapper.listByPage(offset, size, did, status);
            int total = scoreMapper.countAll(did, status);
            return ResultVO.success(new ResultVO.PageResult<>(list, total, page, size));
        }
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

    /** 学生本人成绩：按学期过滤 + GPA 加权平均 + 总学分 */
    @GetMapping("/my")
    public ResultVO my(@RequestParam(required = false) Integer semesterId) {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        return ResultVO.success(scoreService.myScores(sid, semesterId));
    }

    @OperationLogAnnotation("录入成绩")
    @PostMapping
    public ResultVO addScore(@Valid @RequestBody Score score) {
        RoleCheckUtil.requireRole("professor", "admin");
        scoreService.add(score);
        return ResultVO.success();
    }

    @OperationLogAnnotation("修改成绩")
    @PutMapping
    public ResultVO updateScore(@Valid @RequestBody Score score) {
        RoleCheckUtil.requireRole("professor", "admin");
        scoreService.update(score);
        return ResultVO.success();
    }

    @OperationLogAnnotation("删除成绩")
    @DeleteMapping("/{scoreId}")
    public ResultVO deleteScore(@PathVariable Integer scoreId) {
        RoleCheckUtil.requireRole("professor", "admin");
        scoreService.delete(scoreId);
        return ResultVO.success();
    }

    /* ==================== 批次N：成绩流程 ==================== */

    /** 批量提交：草稿 -> 待审核 */
    @OperationLogAnnotation("提交成绩")
    @PostMapping("/submit")
    public ResultVO submit(@RequestBody Map<String, Object> body) {
        RoleCheckUtil.requireRole("professor", "admin");
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("scoreIds");
        int count = scoreService.submit(ids);
        return ResultVO.success("已提交 " + count + " 条成绩");
    }

    /** 批量发布：待审核 -> 已发布，并通知学生 */
    @OperationLogAnnotation("发布成绩")
    @PostMapping("/publish")
    public ResultVO publish(@RequestBody Map<String, Object> body) {
        RoleCheckUtil.requireRole("professor", "admin");
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("scoreIds");
        int count = scoreService.publish(ids);
        return ResultVO.success("已发布 " + count + " 条成绩");
    }

    /** 学生申诉：已发布 -> 复核中 */
    @OperationLogAnnotation("发起成绩复核")
    @PostMapping("/appeal")
    public ResultVO appeal(@RequestBody Map<String, Object> body) {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        Integer scoreId = body.get("scoreId") == null ? null : Integer.valueOf(String.valueOf(body.get("scoreId")));
        String reason = body.get("reason") == null ? "" : String.valueOf(body.get("reason"));
        scoreService.appeal(scoreId, sid, reason);
        return ResultVO.success("复核申请已提交");
    }

    /** 学生本人申诉记录 */
    @GetMapping("/appeal/my")
    public ResultVO appealMy() {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        return ResultVO.success(scoreService.listAppealByStudent(sid));
    }

    /** 管理端待处理申诉 */
    @GetMapping("/appeal/pending")
    public ResultVO appealPending() {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(scoreService.listAppealPending());
    }

    /** 复核处理：approve/reject */
    @OperationLogAnnotation("处理成绩复核")
    @PutMapping("/appeal/handle")
    public ResultVO appealHandle(@RequestBody Map<String, Object> body) {
        RoleCheckUtil.requireRole("professor", "admin");
        Integer appealId = body.get("appealId") == null ? null : Integer.valueOf(String.valueOf(body.get("appealId")));
        String action = body.get("action") == null ? "" : String.valueOf(body.get("action"));
        Double newTotal = body.get("newTotal") == null ? null : Double.valueOf(String.valueOf(body.get("newTotal")));
        String handleNote = body.get("handleNote") == null ? null : String.valueOf(body.get("handleNote"));
        Long uid = JwtInterceptor.UID_HOLDER.get();
        scoreService.handleAppeal(appealId, action, newTotal, handleNote, uid);
        return ResultVO.success();
    }

    @OperationLogAnnotation("导出成绩信息")
    @GetMapping("/export")
    public void exportScore(HttpServletResponse response) throws IOException {
        RoleCheckUtil.requireRole("professor", "admin");
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
