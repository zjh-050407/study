package com.ggs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanCourse {
    private Integer id;
    private Integer planId;
    private Integer courseId;
    /** 课程类型：必修/限选/任选 */
    private String type;
    private Double requireCredit;
    // 关联展示字段
    private String courseName;
    private Double courseCredit;
}