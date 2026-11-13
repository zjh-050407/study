package com.ggs.controller;

import com.ggs.pojo.Admin;
import com.ggs.pojo.RegisterRequest;
import com.ggs.pojo.Student;
import com.ggs.service.AdminService;
import com.ggs.service.CaptchaService;
import com.ggs.service.LoginLimitService;
import com.ggs.service.StudentService;
import com.ggs.util.FileCheckUtil;
import com.ggs.util.JwtUtil;
import com.ggs.vo.LoginVO;
import com.ggs.vo.ResultVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
public class AdminController {

    private static final String LOGIN_FAIL_MSG = "用户名或密码错误，或账号已被临时锁定";

    @Autowired
    private AdminService adminService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private LoginLimitService loginLimitService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${app.upload-path}")
    private String uploadPath;

    @Value("${app.base-url}")
    private String baseUrl;

    /** 图形验证码获取（登录限流配套，无需认证） */
    @GetMapping("/captcha")
    public ResultVO captcha() {
        CaptchaService.MapDto dto = captchaService.issue();
        return ResultVO.success(Map.of("captchaId", dto.captchaId, "img", dto.img));
    }

    /** 登录（安全加固：POST 传凭证 + 失败限流 + 失败≥3次要求验证码 + ≥5次锁定15分钟） */
    @PostMapping("/login")
    public ResultVO login(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String username = str(body.get("username"));
        String password = str(body.get("password"));
        if (username.isEmpty() || password.isEmpty()) {
            return ResultVO.err(LOGIN_FAIL_MSG);
        }
        String ip = clientIp(request);
        if (loginLimitService.isLocked(username, ip)) {
            return ResultVO.err(LOGIN_FAIL_MSG);
        }
        if (loginLimitService.needsCaptcha(username, ip)) {
            String captchaId = str(body.get("captchaId"));
            String captchaCode = str(body.get("captchaCode"));
            if (!captchaService.verify(captchaId, captchaCode)) {
                return ResultVO.err("验证码错误或已过期");
            }
        }

        Admin admin = this.adminService.login(username, password);
        if (admin != null) {
            loginLimitService.clear(username, ip);
            String token = JwtUtil.generateToken(admin.getUid(), admin.getUsername(), admin.getRole());
            LoginVO loginVO = new LoginVO(token, admin.getUid(), admin.getUsername(), admin.getAvatar(), admin.getRole());
            return ResultVO.success(loginVO);
        }
        // 教师/管理员登录失败后回退学生账号登录（学号或学生用户名）；统一失败提示防账号枚举
        Student stu = studentService.login(username, password);
        if (stu != null) {
            loginLimitService.clear(username, ip);
            String token = JwtUtil.generateToken(Long.valueOf(stu.getSid()), stu.getName(), "student");
            LoginVO loginVO = new LoginVO(token, Long.valueOf(stu.getSid()), stu.getName(), stu.getAvatar(), "student");
            return ResultVO.success(loginVO);
        }
        loginLimitService.recordFail(username, ip);
        return ResultVO.err(LOGIN_FAIL_MSG);
    }

    /** 登出：将当前 token 加入黑名单直至其自然过期（安全加固 P2） */
    @PostMapping("/logout")
    public ResultVO logout(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            long remain = JwtUtil.remainingMillis(token);
            if (remain > 0) {
                redisTemplate.opsForValue().set(JwtUtil.blacklistKey(token), "1", java.time.Duration.ofMillis(remain));
            }
        }
        return ResultVO.success();
    }

    private String str(Object v) {
        return v == null ? "" : String.valueOf(v).trim();
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /** 修改密码（安全加固：凭证走 body，杜绝 URL 日志泄漏） */
    @PutMapping("/password")
    public ResultVO updatePassword(@RequestBody Map<String, Object> body) {
        Long uid = body.get("uid") == null ? null : Long.valueOf(String.valueOf(body.get("uid")));
        String oldPassword = str(body.get("oldPassword"));
        String newPassword = str(body.get("newPassword"));
        if (uid == null || oldPassword.isEmpty() || newPassword.isEmpty()) {
            return ResultVO.err("参数不完整");
        }
        Admin admin = adminService.getById(uid);
        if (admin == null || !adminService.login(admin.getUsername(), oldPassword).equals(admin)) {
            return ResultVO.err("原密码错误");
        }
        adminService.updatePassword(uid, newPassword);
        return ResultVO.success();
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResultVO upload(@RequestParam("multipartFile") MultipartFile multipartFile) {
        // 安全加固：类型/大小/文件名校验，服务端随机文件名
        String safeName = FileCheckUtil.checkImage(multipartFile);
        // 绝对化路径，避免相对路径随 JVM 工作目录漂移导致目录创建/写入不一致
        File uploadDir = new File(uploadPath).getAbsoluteFile();
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        File dist = new File(uploadDir, safeName);
        try {
            multipartFile.transferTo(dist);
            String url = baseUrl + safeName;
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