package com.newlimange.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 动漫观看状态参数
 * 接收前端数据实体
 * by shengjing19(Hisx12123)
 * created 2026-01-02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsDTO {
    // X轴标签 (如: ["周一", "周二"] 或 ["1月", "2月"])
    private List<String> labels;

    // Y轴数值 (如: [1, 0, 5, ...])
    private List<Integer> values;
}