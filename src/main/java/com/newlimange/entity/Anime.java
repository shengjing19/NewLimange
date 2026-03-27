package com.newlimange.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 动漫实体类
 * by shengjing19(Hisx12123)
 * created 2026-01-03
 */
@Data
@Entity
@Table(name = "animes")
public class Anime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "cover_image", nullable = false)
    private String coverImage;

    @Column(name = "total_episodes")
    private Integer totalEpisodes;

    @Column(name = "finish_date")
    private LocalDate finishDate; // JPA自动转换日期

    @Column(name = "is_favorite")
    private Boolean isFavorite;

    @Column(name = "is_watching")
    private Boolean isWatching;

    @Column(name = "`describe`", length = 500) // describe 是关键字，需要转义
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}