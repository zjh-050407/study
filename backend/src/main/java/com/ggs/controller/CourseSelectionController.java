package com.ggs.controller;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.interceptor.JwtInterceptor;
import com.ggs.pojo.CourseSelection;
import com.ggs.service.CourseSelectionService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/selection")
public class CourseSelectionController {

    @Autowired
    private CourseSelectionService selectionService;

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

    /** 可选课程列表（带已选标记） */
    @GetMapping("/self/courses")
    public ResultVO selfCourses() {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        List<Map<String, Object>> list = selectionService.listCourseOptions(sid);
        return ResultVO.success(list);
    }

    /** 本人已选课程 */
    @GetMapping("/self/list")
    public ResultVO selfList() {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        return ResultVO.success(selectionService.listByStudent(sid));
    }

    /** 自助选课（sid 与 token 绑定，复用冲突/重复校验） */
    @OperationLogAnnotation("学生自助选课")
    @PostMapping("/self")
    public ResultVO selfAdd(@RequestBody CourseSelection selection) {
        RoleCheckUtil.requireRole("student");
        Long uid = JwtInterceptor.UID_HOLDER.get();
        int sid = uid == null ? -1 : uid.intValue();
        selectionService.add(sid, selection.getCourseId());
        return ResultVO.success();
    }
}