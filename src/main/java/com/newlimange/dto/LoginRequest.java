package com.newlimange.dto;

import lombok.Data;

/**
 * 封装前端发来的登录参数
 * by shengjing19(Hisx12123)
 * created 2025-12-27
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
}
