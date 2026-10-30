package com.ggs.service;

import com.ggs.pojo.Admin;
import com.ggs.pojo.RegisterRequest;

public interface AdminService {

    Admin login(String username, String password);

    int avatar(long uid, String avatar);

    Admin getById(Long uid);

    int updatePassword(Long uid, String password);

    RegisterRequest register(String username, String password, String role);
}