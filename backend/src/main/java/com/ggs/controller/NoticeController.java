package com.ggs.controller;

import com.ggs.annotation.OperationLogAnnotation;
import com.ggs.pojo.Notice;
import com.ggs.service.NoticeService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @GetMapping("/list")
    public ResultVO list(@RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size) {
        return ResultVO.success(noticeService.listNotice(page, size));
    }

    @GetMapping("/latest")
    public ResultVO latest(@RequestParam(defaultValue = "5") int limit) {
        if (limit < 1) {
            limit = 5;
        }
        List<Notice> list = noticeService.latest(limit);
        return ResultVO.success(list);
    }

    @OperationLogAnnotation("发布公告")
    @PostMapping
    public ResultVO add(@RequestBody Notice notice) {
        RoleCheckUtil.requireRole("professor", "admin");
        noticeService.add(notice);
        return ResultVO.success();
    }

    @OperationLogAnnotation("删除公告")
    @DeleteMapping("/{id}")
    public ResultVO delete(@PathVariable Integer id) {
        RoleCheckUtil.requireRole("professor", "admin");
        noticeService.delete(id);
        return ResultVO.success();
    }
}