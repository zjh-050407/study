package com.ggs.controller;

import com.ggs.pojo.RegisterRequest;
import com.ggs.service.RegisterRequestService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/register")
public class RegisterRequestController {

    @Autowired
    private RegisterRequestService registerRequestService;

    @GetMapping("/requests")
    public ResultVO listRequests() {
        RoleCheckUtil.requireRole("admin");
        List<RegisterRequest> list = registerRequestService.listPending();
        return ResultVO.success(list);
    }

    @PostMapping("/approve")
    public ResultVO approve(@RequestParam Integer id) {
        RoleCheckUtil.requireRole("admin");
        registerRequestService.approve(id);
        return ResultVO.success("审批通过");
    }

    @PostMapping("/reject")
    public ResultVO reject(@RequestParam Integer id) {
        RoleCheckUtil.requireRole("admin");
        registerRequestService.reject(id);
        return ResultVO.success("已拒绝");
    }
}