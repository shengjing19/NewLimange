package com.newlimange.dto;

import lombok.Data;

/**
 * 封装前端发来的注册信息(保留)
 * by shengjing19(Hisx12123)
 * created 2025-12-27
 */
@Data
public class RegisterRequest {
    private String username;
    private String password;
}