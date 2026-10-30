package com.ggs.controller;

import com.ggs.service.InitService;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/init")
public class InitController {

    @Autowired
    private InitService initService;

    @PostMapping("/reset")
    public ResultVO reset() {
        RoleCheckUtil.requireRole("admin");
        initService.resetDatabase();
        return ResultVO.success("系统初始化完成");
    }
}
