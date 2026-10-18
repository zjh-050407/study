package com.ggs.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultVO<T> {
    private Integer code;
    private String message;
    private T data;

    public static ResultVO success() {
        return new ResultVO(0, "操作成功", null);
    }

    public static <T> ResultVO success(T data) {
        return new ResultVO(0, "操作成功", data);
    }

    public static ResultVO err(String err) {
        return new ResultVO(1, err, null);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PageResult<T> {
        private List<T> list;
        private int total;
        private int page;
        private int size;
    }
}