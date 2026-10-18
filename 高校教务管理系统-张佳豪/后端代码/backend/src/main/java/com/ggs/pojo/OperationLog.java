package com.ggs.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationLog {
    private Long id;
    private String username;
    private String operation;
    private String method;
    private String params;
    private String ip;
    private LocalDateTime createTime;
}
