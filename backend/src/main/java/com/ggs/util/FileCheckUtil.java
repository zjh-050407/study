package com.ggs.util;

import com.ggs.exception.BusinessException;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 上传文件安全校验（批次安全加固）：
 * - 扩展名白名单 + 大小上限 + 服务端随机文件名 + 拒绝路径穿越
 */
public class FileCheckUtil {

    private static final List<String> IMAGE_EXT = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> EXCEL_EXT = Arrays.asList("xlsx", "xls");
    private static final long IMAGE_MAX = 5 * 1024 * 1024L;   // 5MB
    private static final long EXCEL_MAX = 10 * 1024 * 1024L;  // 10MB

    /** 校验图片上传并返回安全随机文件名（含扩展名） */
    public static String checkImage(MultipartFile file) {
        return check(file, IMAGE_EXT, IMAGE_MAX, "图片");
    }

    /** 校验 Excel 上传并返回安全随机文件名（含扩展名） */
    public static String checkExcel(MultipartFile file) {
        return check(file, EXCEL_EXT, EXCEL_MAX, "表格");
    }

    private static String check(MultipartFile file, List<String> allowedExt, long maxSize, String label) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(label + "文件过大，最大支持 " + (maxSize / 1024 / 1024) + "MB");
        }
        String original = file.getOriginalFilename();
        if (!StringUtils.hasText(original)) {
            throw new BusinessException("文件名不合法");
        }
        // 拒绝路径穿越与绝对路径（Windows/Unix 分隔符、盘符、..）
        if (original.contains("..") || original.contains("/") || original.contains("\\")
                || original.matches("^[a-zA-Z]:.*")) {
            throw new BusinessException("文件名不合法");
        }
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "";
        if (!allowedExt.contains(ext)) {
            throw new BusinessException(label + "格式仅支持：" + String.join("/", allowedExt));
        }
        // 服务端生成随机文件名，杜绝与原始文件名拼接
        return UUID.randomUUID().toString().replace("-", "") + "." + ext;
    }
}
