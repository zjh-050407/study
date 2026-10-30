package com.ggs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notice {
    private Integer id;
    private String title;
    private String content;
    private String type;
    private String targetRole;
    private Integer isTop;
    private Integer status;
    private String publisher;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
}