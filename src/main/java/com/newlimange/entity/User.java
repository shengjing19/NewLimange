package com.newlimange.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * 用户实体类，实现UserDetails 接口
 * by shengjing19(Hisx12123)
 * created 2025-12-27
 */
@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer uid; // 对应数据库 uid

    @Column(unique = true, nullable = false, length = 10)
    private String username;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 返回空,默认USER权限
        return Collections.emptyList();
    }

    /**
     * 以下字段保留默认true
     * 账号是否未过期？
     * 账号是否未被锁定？
     * 凭证（密码）是否未过期？
     * 账号是否可用（已启用）？
     * */
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}