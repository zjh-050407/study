package com.ggs.controller;

import com.ggs.pojo.OperationLog;
import com.ggs.service.OperationLogService;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/operationLog")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/list")
    public ResultVO list() {
        List<OperationLog> list = operationLogService.list();
        return ResultVO.success(list);
    }
}
