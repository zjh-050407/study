package com.ggs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingPlan {
    private Integer id;
    /** 适用班级 clazz.cid */
    private Integer cid;
    private String name;
    private Double totalCreditRequired;
    private Double requiredCreditRequired;
    private Double electiveCreditRequired;
    // 关联展示字段
    private String clazzName;
}