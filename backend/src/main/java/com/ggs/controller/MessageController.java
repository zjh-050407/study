package com.ggs.controller;

import com.ggs.interceptor.JwtInterceptor;
import com.ggs.service.MessageService;
import com.ggs.util.PageUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 站内消息 + 待办（批次M）：角标一次拉齐（个人未读消息数 + 未读公告数 + 待办聚合）
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    private String receiverType() {
        return "student".equals(JwtInterceptor.ROLE_HOLDER.get()) ? "student" : "admin";
    }

    private Long receiverId() {
        Long uid = JwtInterceptor.UID_HOLDER.get();
        return uid == null ? 0L : uid;
    }

    private String userKey() {
        String role = JwtInterceptor.ROLE_HOLDER.get();
        return (role == null ? "anon" : role) + ":" + receiverId();
    }

    /** 角标数据：未读消息数 + 未读公告数 + 待办聚合 */
    @GetMapping("/unreadCount")
    public ResultVO unreadCount() {
        Map<String, Object> map = new HashMap<>();
        map.put("messageUnread", messageService.unreadCount(receiverType(), receiverId()));
        map.put("noticeUnread", messageService.noticeUnread(userKey()));
        map.put("todo", messageService.todoSummary(JwtInterceptor.ROLE_HOLDER.get()));
        return ResultVO.success(map);
    }

    @GetMapping("/list")
    public ResultVO list(@RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size) {
        page = PageUtil.clampPage(page);
        size = PageUtil.clampSize(size);
        ResultVO.PageResult<com.ggs.pojo.Message> pageResult = new ResultVO.PageResult<>(
                messageService.list(receiverType(), receiverId(), page, size),
                messageService.count(receiverType(), receiverId()), page, size);
        return ResultVO.success(pageResult);
    }

    @PutMapping("/{id}/read")
    public ResultVO read(@PathVariable Long id) {
        messageService.markRead(id, receiverType(), receiverId());
        return ResultVO.success();
    }

    @PutMapping("/readAll")
    public ResultVO readAll() {
        messageService.markAllRead(receiverType(), receiverId());
        return ResultVO.success();
    }

    /** 公告已读：游标推进到最新公告ID */
    @PutMapping("/noticeRead")
    public ResultVO noticeRead() {
        messageService.markNoticeRead(userKey());
        return ResultVO.success();
    }
}
