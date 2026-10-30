package com.ggs.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginVO {
    private String token;
    private Long uid;
    private String username;
    private String avatar;
    private String role;
}