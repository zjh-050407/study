package com.ggs.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

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
}