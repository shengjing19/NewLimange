package com.newlimange.controller;

import com.newlimange.dto.LoginRequest;
import com.newlimange.dto.LoginResponse;
import com.newlimange.dto.RegisterRequest; // 导入新DTO
import com.newlimange.entity.User;
import com.newlimange.repository.UserRepository;
import com.newlimange.utils.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * limange主登录控制器
 * by shengjing19(Hisx12123)
 * created 2025-12-27
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    // 注入依赖
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // 使用 AuthenticationManager 进行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 认证成功，生成 Token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtils.generateToken(userDetails);

            // 返回 Token 给前端
            return ResponseEntity.ok(new LoginResponse(token, userDetails.getUsername()));

        } catch (Exception e) {
            // 认证失败 (密码错误或用户不存在)
            return ResponseEntity.status(401).body("用户名或密码错误");
        }
    }

    /**
     * 注册接口 (保留)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        boolean isOn = false;   // 是否开启注册功能的开关(硬关闭)
        if(isOn){
            // 检查用户名是否已存在
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body("用户名已存在");
            }

            // 创建新用户对象
            User user = new User();
            user.setUsername(request.getUsername());

            // 使用 BCrypt 加密密码
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            // 保存到数据库
            userRepository.save(user);

            return ResponseEntity.ok("注册成功，请登录");
        }
        else{
            return ResponseEntity.ok("服务器已关闭注册");
        }

    }

    /**
     * 登出接口
     * tips:JWT 是无状态的，服务端无法"强制"登出。
     * "登出"的本质是前端丢弃 Token。
     * 这个接口主要用于前端通知后端，或者留作扩展(例如加入 Token 黑名单)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // 在无状态 JWT 模式下，这里什么都不用做
        // 真正的登出逻辑在前端：localStorage.removeItem("token")
        return ResponseEntity.ok("登出成功");
    }
}
