package com.ggs.service;

import com.ggs.mapper.AdminMapper;
import com.ggs.mapper.RegisterRequestMapper;
import com.ggs.pojo.Admin;
import com.ggs.pojo.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegisterRequestServiceImpl implements RegisterRequestService {

    @Autowired
    private RegisterRequestMapper registerRequestMapper;

    @Autowired
    private AdminMapper adminMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public RegisterRequest submitRequest(String username, String password, String role) {
        Admin existAdmin = adminMapper.findByUsername(username);
        if (existAdmin != null) {
            return null;
        }
        RegisterRequest existRequest = registerRequestMapper.findByUsername(username);
        if (existRequest != null) {
            return null;
        }
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword(passwordEncoder.encode(password));
        request.setRole(role);
        request.setStatus("pending");
        registerRequestMapper.insert(request);
        return request;
    }

    @Override
    public List<RegisterRequest> listPending() {
        return registerRequestMapper.selectByStatus("pending");
    }

    @Override
    @Transactional
    public void approve(Integer id) {
        RegisterRequest request = registerRequestMapper.selectById(id);
        if (request == null || !"pending".equals(request.getStatus())) {
            throw new RuntimeException("申请不存在或已处理");
        }
        registerRequestMapper.updateStatus(id, "approved");

        Admin admin = new Admin();
        admin.setUsername(request.getUsername());
        admin.setPassword(request.getPassword());
        admin.setRole(request.getRole());
        adminMapper.insertNextUid(admin);
    }

    @Override
    public void reject(Integer id) {
        RegisterRequest request = registerRequestMapper.selectById(id);
        if (request == null || !"pending".equals(request.getStatus())) {
            throw new RuntimeException("申请不存在或已处理");
        }
        registerRequestMapper.updateStatus(id, "rejected");
    }
}