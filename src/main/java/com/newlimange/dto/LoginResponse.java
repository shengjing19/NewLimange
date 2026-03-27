package com.newlimange.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 封装返回给前端的登录(Token)参数
 * by shengjing19(Hisx12123)
 * created 2025-12-27
 */
@Data
@AllArgsConstructor // 引入 lombok
public class LoginResponse {
    private String token;
    private String username;
}