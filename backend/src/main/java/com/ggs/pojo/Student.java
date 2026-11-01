package com.ggs.pojo;

import com.alibaba.excel.annotation.ExcelIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    private Integer sid;
    private String username;
    @ExcelIgnore
    private String password;
    private Integer status;
    private String name;
    private Integer age;
    private String sex;
    private String avatar;
    private String email;
    private String phone;
    private Integer cid;
    // 关联字段
    private String clazzName;
    private String deptName;
}
