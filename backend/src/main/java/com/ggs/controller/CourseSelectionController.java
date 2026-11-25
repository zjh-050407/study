package com.ggs.controller;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.exception.BusinessException;
import com.ggs.interceptor.JwtInterceptor;
import com.ggs.pojo.CourseSelection;
import com.ggs.pojo.SelectionRound;
import com.ggs.service.CourseSelectionService;
import com.ggs.service.SelectionRoundService;
import com.ggs.service.StatusChangeService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/selection")
public class CourseSelectionController {

    @Autowired
    private CourseSelectionService selectionService;

    @Autowired
    private SelectionRoundService roundService;

    @Autowired
    private StatusChangeService statusChangeService;

    @GetMapping("/list")
    public ResultVO list() {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(selectionService.listAll());
    }

    @GetMapping("/listByStudent")
    public ResultVO listByStudent(@RequestParam Integer sid) {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(selectionService.listByStudent(sid));
    }

    @OperationLogAnnotation("代学生选课")
    @PostMapping
    public ResultVO add(@RequestBody CourseSelection selection) {
        RoleCheckUtil.requireRole("professor", "admin");
        selectionService.add(selection.getSid(), selection.getCourseId());
        return ResultVO.success();
    }

    @OperationLogAnnotation("删除选课记录")
    @DeleteMapping("/{id}")
    public ResultVO delete(@PathVariable Integer id) {
        RoleCheckUtil.requireRole("professor", "admin");
        selectionService.delete(id);
        return ResultVO.success();
    }

    /* ==================== 学生自助选课 ==================== */

    /** 可选课程列表（带轮次信息：初选提交志愿 / 补退选即时抢课） */
    @GetMapping("/self/courses")
    public ResultVO selfCourses() {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        Map<String, Object> map = new HashMap<>();
        map.put("round", roundService.current());
        map.put("list", selectionService.listCourseOptions(sid));
        return ResultVO.success(map);
    }

    /** 本人已选课程 */
    @GetMapping("/self/list")
    public ResultVO selfList() {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        return ResultVO.success(selectionService.listByStudent(sid));
    }

    /** 自助选课（仅补退选轮次进行中可用；sid 与 token 绑定，复用冲突/重复校验） */
    @OperationLogAnnotation("学生自助选课")
    @PostMapping("/self")
    public ResultVO selfAdd(@RequestBody CourseSelection selection) {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        // 校验顺序：学籍状态（最强约束） -> 已选 -> 选课轮次
        String blocked = statusChangeService.blockedStatus(sid);
        if (blocked != null) {
            throw new BusinessException("该学生当前学籍状态为【" + blocked + "】，无法选课");
        }
        if (selectionService.hasSelected(sid, selection.getCourseId())) {
            throw new BusinessException("该学生已选此课程，请勿重复选课");
        }
        Map<String, Object> current = roundService.current();
        Object second = current.get("second");
        if (second == null) {
            throw new BusinessException("当前没有进行中的补退选轮次，无法自助选课");
        }
        selectionService.add(sid, selection.getCourseId());
        return ResultVO.success();
    }

    /* ==================== 批次O：选课轮次 ==================== */

    @GetMapping("/round/list")
    public ResultVO roundList(@RequestParam(required = false) Integer semesterId) {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(roundService.list(semesterId));
    }

    @OperationLogAnnotation("创建选课轮次")
    @PostMapping("/round")
    public ResultVO roundCreate(@RequestBody SelectionRound round) {
        RoleCheckUtil.requireRole("professor", "admin");
        roundService.create(round);
        return ResultVO.success();
    }

    @OperationLogAnnotation("轮次状态流转")
    @PutMapping("/round/{id}/status")
    public ResultVO roundStatus(@PathVariable Integer id, @RequestParam Integer status) {
        RoleCheckUtil.requireRole("professor", "admin");
        roundService.updateStatus(id, status);
        return ResultVO.success();
    }

    @OperationLogAnnotation("初选投档")
    @PostMapping("/round/{id}/allocate")
    public ResultVO roundAllocate(@PathVariable Integer id) {
        RoleCheckUtil.requireRole("professor", "admin");
        return ResultVO.success(roundService.allocate(id));
    }

    /** 学生端当前进行中轮次 */
    @GetMapping("/round/current")
    public ResultVO roundCurrent() {
        return ResultVO.success(roundService.current());
    }

    /** 学生提交志愿（覆盖式） */
    @OperationLogAnnotation("提交选课志愿")
    @PostMapping("/wish")
    public ResultVO wishSubmit(@RequestBody Map<String, Object> body) {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        Integer roundId = body.get("roundId") == null ? null : Integer.valueOf(String.valueOf(body.get("roundId")));
        @SuppressWarnings("unchecked")
        List<Integer> courseIds = (List<Integer>) body.get("courseIds");
        roundService.submitWishes(sid, roundId, courseIds);
        return ResultVO.success();
    }

    /** 学生本人志愿 */
    @GetMapping("/wish/my")
    public ResultVO wishMy(@RequestParam Integer roundId) {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        return ResultVO.success(roundService.listWishes(sid, roundId));
    }
}