package com.ggs.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

@Component
public class JwtUtil {

    /** 旧版硬编码默认密钥，启动时显式拒绝使用 */
    private static final String FORBIDDEN_LEGACY_SECRET = "Vx8#pQ2@zL7!mR4%tN6&kY9(wC3^fH5)dB1*sJ0%eG8";

    private static String SECRET;
    private static long EXPIRE = 24 * 60 * 60 * 1000L;

    @Value("${app.jwt.secret}")
    public void setSecret(String secret) {
        JwtUtil.SECRET = secret;
    }

    @Value("${app.jwt.expire:86400000}")
    public void setExpire(long expire) {
        JwtUtil.EXPIRE = expire;
    }

    /** 安全加固：启动时校验密钥，未注入/过短/使用公开默认值一律拒绝启动 */
    @PostConstruct
    public void validateSecret() {
        if (SECRET == null || SECRET.trim().isEmpty()) {
            throw new IllegalStateException("JWT_SECRET 未设置：请通过环境变量注入至少32字节的随机密钥（例如 PowerShell: $env:JWT_SECRET='<随机密钥>'），不得使用仓库中的公开默认值");
        }
        if (SECRET.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET 长度不足32字节，请使用更长的随机密钥（可用 openssl rand -base64 48 生成）");
        }
        if (FORBIDDEN_LEGACY_SECRET.equals(SECRET)) {
            throw new IllegalStateException("JWT_SECRET 使用了已公开的默认密钥，存在伪造令牌风险，请立即更换为随机密钥");
        }
    }

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(Long uid, String username) {
        return generateToken(uid, username, null);
    }

    public static String generateToken(Long uid, String username, String role) {
        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(uid))
                .claim("username", username)
                // 安全加固：jti 唯一标识，避免同一毫秒内重复登录生成相同 token 导致登出黑名单误伤
                .id(java.util.UUID.randomUUID().toString().replace("-", ""))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE));
        if (role != null) {
            builder.claim("role", role);
        }
        return builder.signWith(getKey()).compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static boolean isExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /** 返回 token 剩余有效毫秒数（已过期/非法返回 0） */
    public static long remainingMillis(String token) {
        try {
            long remain = parseToken(token).getExpiration().getTime() - System.currentTimeMillis();
            return Math.max(remain, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    /** 黑名单键：对 token 做 SHA-256 摘要，避免原文落入 Redis */
    public static String blacklistKey(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("jwt:bl:");
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("黑名单键生成失败", e);
        }
    }
}