package com.ggs.controller;

import com.ggs.pojo.Admin;
import com.ggs.pojo.RegisterRequest;
import com.ggs.service.AdminService;
import com.ggs.util.JwtUtil;
import com.ggs.vo.LoginVO;
import com.ggs.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Value("${app.upload-path}")
    private String uploadPath;

    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping("/login")
    public ResultVO login(String username, String password) {
        Admin admin = this.adminService.login(username, password);
        if (admin == null) {
            return ResultVO.err("用户名或密码错误");
        }
        String token = JwtUtil.generateToken(admin.getUid(), admin.getUsername());
        LoginVO loginVO = new LoginVO(token, admin.getUid(), admin.getUsername(), admin.getAvatar(), admin.getRole());
        return ResultVO.success(loginVO);
    }

    @PutMapping("/password")
    public ResultVO updatePassword(@RequestParam Long uid,
                                   @RequestParam String oldPassword,
                                   @RequestParam String newPassword) {
        Admin admin = adminService.getById(uid);
        if (admin == null || !adminService.login(admin.getUsername(), oldPassword).equals(admin)) {
            return ResultVO.err("原密码错误");
        }
        adminService.updatePassword(uid, newPassword);
        return ResultVO.success();
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResultVO upload(@RequestParam("multipartFile") MultipartFile multipartFile) {
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        String filename = multipartFile.getOriginalFilename();
        String uniqueName = System.currentTimeMillis() + "_" + filename;
        String dist = uploadPath + uniqueName;
        try {
            multipartFile.transferTo(new File(dist));
            String url = baseUrl + uniqueName;
            return ResultVO.success(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PatchMapping("/avatar")
    public ResultVO avatar(long uid, String avatar) {
        int isUpdate = this.adminService.avatar(uid, avatar);
        if (isUpdate == 1) {
            return ResultVO.success();
        }
        return ResultVO.err("更新失败");
    }

    @PostMapping("/register")
    public ResultVO register(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String role) {
        RegisterRequest request = adminService.register(username, password, role);
        if (request == null) {
            return ResultVO.err("用户名已存在或已有待审批申请");
        }
        return ResultVO.success("注册申请已提交，请等待管理员审批");
    }
}