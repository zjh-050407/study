package com.ggs.util;

/**
 * 分页参数收敛（安全加固 P1）：防止 size 被放大为全表拖取。
 */
public class PageUtil {

    public static final int MAX_PAGE_SIZE = 100;

    public static int clampPage(int page) {
        return Math.max(1, page);
    }

    public static int clampSize(int size) {
        return Math.max(1, Math.min(size, MAX_PAGE_SIZE));
    }
}
