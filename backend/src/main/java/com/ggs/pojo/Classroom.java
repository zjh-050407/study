package com.ggs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Classroom {
    private Integer id;
    private String name;
    private Integer capacity;
    private String type;
    private String building;
    private Integer status;
    private LocalDateTime createTime;
}
