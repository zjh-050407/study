package com.ggs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    private Integer sid;
    private String name;
    private Integer age;
    private String sex;
    private Integer cid;
    // 关联字段
    private String clazzName;
    private String deptName;
}
