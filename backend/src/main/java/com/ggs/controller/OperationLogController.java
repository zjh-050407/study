package com.ggs.controller;

import com.ggs.mapper.OperationLogMapper;
import com.ggs.pojo.OperationLog;
import com.ggs.service.OperationLogService;
import com.ggs.util.PageUtil;
import com.ggs.util.RoleCheckUtil;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/operationLog")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private OperationLogMapper operationLogMapper;

    @GetMapping("/list")
    public ResultVO list(@RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) String username,
                         @RequestParam(required = false) String operation,
                         @RequestParam(required = false) String startTime,
                         @RequestParam(required = false) String endTime) {
        RoleCheckUtil.requireRole("admin");
        page = PageUtil.clampPage(page);
        size = PageUtil.clampSize(size);
        int offset = (page - 1) * size;
        List<OperationLog> list = operationLogMapper.listByPage(offset, size, username, operation, startTime, endTime);
        int total = operationLogMapper.countByFilter(username, operation, startTime, endTime);
        return ResultVO.success(new ResultVO.PageResult<>(list, total, page, size));
    }
}
