package com.ggs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

/**
 * 图形验证码（安全加固 P1）：纯 Java 绘制，无外部依赖。
 * 验证码存 Redis：captcha:{uuid} -> code，TTL 5 分钟，校验通过一次性删除。
 */
@Service
public class CaptchaService {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Duration TTL = Duration.ofMinutes(5);

    @Autowired
    private StringRedisTemplate redisTemplate;

    public MapDto issue() {
        String code = randomCode(4);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("captcha:" + uuid, code, TTL);
        BufferedImage image = draw(code);
        return new MapDto(uuid, toDataUrl(image));
    }

    /** 校验并消费验证码（不区分大小写）；错误/过期返回 false */
    public boolean verify(String uuid, String input) {
        if (uuid == null || input == null || input.trim().isEmpty()) {
            return false;
        }
        String key = "captcha:" + uuid;
        String code = redisTemplate.opsForValue().get(key);
        if (code == null) {
            return false;
        }
        // 一次性使用：无论对错都删除
        redisTemplate.delete(key);
        return code.equalsIgnoreCase(input.trim());
    }

    private String randomCode(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private BufferedImage draw(String code) {
        int w = 120, h = 44;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(245, 248, 252));
        g.fillRect(0, 0, w, h);
        // 干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(150 + RANDOM.nextInt(90), 170 + RANDOM.nextInt(70), 210 + RANDOM.nextInt(40)));
            g.drawLine(RANDOM.nextInt(w), RANDOM.nextInt(h), RANDOM.nextInt(w), RANDOM.nextInt(h));
        }
        g.setFont(new Font("Arial", Font.BOLD, 26));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(30 + RANDOM.nextInt(80), 60 + RANDOM.nextInt(90), 140 + RANDOM.nextInt(100)));
            g.drawString(String.valueOf(code.charAt(i)), 14 + i * 26, 32);
        }
        g.dispose();
        return img;
    }

    private String toDataUrl(BufferedImage image) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("验证码生成失败", e);
        }
    }

    public static class MapDto {
        public final String captchaId;
        public final String img;

        public MapDto(String captchaId, String img) {
            this.captchaId = captchaId;
            this.img = img;
        }
    }
}
