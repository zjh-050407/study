package com.ggs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectionWish {

    private Integer id;

    private Integer roundId;

    private Integer sid;

    private Integer courseId;

    /** 志愿序号 1..N */
    private Integer priority;

    private LocalDateTime createTime;

    // 关联展示
    private String courseName;
    private String teacherName;
    private Integer capacity;
    private Integer selectedCount;
}
