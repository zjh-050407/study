package com.ggs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectionRound {

    private Integer id;

    /** 学期（0=未分学期哨兵） */
    private Integer semesterId;

    private String name;

    /** 1初选(志愿投档)/2补退选(先到先得) */
    private Integer type;

    /** 0未开始/1进行中/2已结束 */
    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 初选每人志愿上限 */
    private Integer maxWishes;

    private LocalDateTime createTime;
}
