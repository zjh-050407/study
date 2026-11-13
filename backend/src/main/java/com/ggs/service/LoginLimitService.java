package com.ggs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 登录防爆破限流（安全加固 P1）：
 * - 失败计数：login:fail:{username} 与 login:fail:ip:{ip}，15 分钟窗口；
 * - 失败 ≥5 次锁定 15 分钟；失败 ≥3 次要求图形验证码；
 * - 登录成功清零计数。
 */
@Service
public class LoginLimitService {

    private static final int CAPTCHA_THRESHOLD = 3;
    private static final int LOCK_THRESHOLD = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String userKey(String username) {
        return "login:fail:" + username;
    }

    private String ipKey(String ip) {
        return "login:fail:ip:" + ip;
    }

    private int current(String key) {
        String v = redisTemplate.opsForValue().get(key);
        return v == null ? 0 : Integer.parseInt(v);
    }

    public void recordFail(String username, String ip) {
        Long c1 = redisTemplate.opsForValue().increment(userKey(username));
        if (c1 != null && c1 == 1) {
            redisTemplate.expire(userKey(username), WINDOW);
        }
        if (ip != null && !ip.isEmpty()) {
            Long c2 = redisTemplate.opsForValue().increment(ipKey(ip));
            if (c2 != null && c2 == 1) {
                redisTemplate.expire(ipKey(ip), WINDOW);
            }
        }
    }

    public void clear(String username, String ip) {
        redisTemplate.delete(userKey(username));
        if (ip != null && !ip.isEmpty()) {
            redisTemplate.delete(ipKey(ip));
        }
    }

    public boolean isLocked(String username, String ip) {
        return current(userKey(username)) >= LOCK_THRESHOLD
                || (ip != null && !ip.isEmpty() && current(ipKey(ip)) >= LOCK_THRESHOLD);
    }

    public boolean needsCaptcha(String username, String ip) {
        return current(userKey(username)) >= CAPTCHA_THRESHOLD
                || (ip != null && !ip.isEmpty() && current(ipKey(ip)) >= CAPTCHA_THRESHOLD);
    }
}
