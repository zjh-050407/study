package com.ggs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Clazz {
    private Integer cid;
    private String name;
    private Integer did;
    private Integer tid;
    private Integer capacity;
    // 关联字段
    private String deptName;
    private String teacherName;
}
