package com.ggs.service;

import com.ggs.mapper.OperationLogMapper;
import com.ggs.pojo.OperationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public List<OperationLog> list() {
        return operationLogMapper.list();
    }
}
