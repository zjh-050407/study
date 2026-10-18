package com.ggs.service;

import com.ggs.mapper.AdminMapper;
import com.ggs.pojo.Admin;
import com.ggs.pojo.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private RegisterRequestService registerRequestService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Admin login(String username, String password) {
        Admin admin = adminMapper.findByUsername(username);
        if (admin == null) {
            return null;
        }
        String stored = admin.getPassword();
        // Compatible with plain text passwords in existing database
        if (stored != null && !stored.startsWith("$2a$") && stored.equals(password)) {
            return admin;
        }
        if (passwordEncoder.matches(password, stored)) {
            return admin;
        }
        return null;
    }

    @Override
    public int avatar(long uid, String avatar) {
        return adminMapper.updateAvatar(uid, avatar);
    }

    @Override
    public Admin getById(Long uid) {
        return adminMapper.getById(uid);
    }

    @Override
    public int updatePassword(Long uid, String password) {
        String encoded = passwordEncoder.encode(password);
        return adminMapper.updatePassword(uid, encoded);
    }

    @Override
    public RegisterRequest register(String username, String password, String role) {
        return registerRequestService.submitRequest(username, password, role);
    }
}