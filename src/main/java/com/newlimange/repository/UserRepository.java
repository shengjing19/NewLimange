package com.newlimange.repository;

import com.newlimange.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * 用户数据仓库(从数据库中获取或存储数据)
 * by shengjing19(Hisx12123)
 * created 2025-12-27
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    /**
     * 通过用户名查找用户
     * */
    Optional<User> findByUsername(String username);
}