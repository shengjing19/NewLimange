package com.newlimange.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank; // Spring Boot 3.x 使用 jakarta 包
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 动漫表单参数（数据管理）
 * 接收前端表单数据实体
 * by shengjing19(Hisx12123)
 * created 2026-01-02
 */
@Data
public class AnimeDTO {

    /**
     * 动漫ID (仅在更新/删除时需要)
     */
    private Integer id;

    /**
     * 动漫标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 总集数
     */
    @NotNull(message = "集数不能为空")
    private Integer episodes;

    /**
     * 看完日期
     * 使用 @DateTimeFormat 自动将前端的 "yyyy-MM-dd" 字符串转为 LocalDate
     */
    @NotNull(message = "日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate finishDate;

    /**
     * 是否最喜欢
     */
    private Boolean isFavorite = false;

    /**
     * 是否正在追
     */
    private Boolean isWatching = false;

    /**
     * 描述
     */
    private String description;
}