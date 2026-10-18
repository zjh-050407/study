package com.ggs.service;

import com.ggs.pojo.RegisterRequest;

import java.util.List;

public interface RegisterRequestService {

    RegisterRequest submitRequest(String username, String password, String role);

    List<RegisterRequest> listPending();

    void approve(Integer id);

    void reject(Integer id);
}