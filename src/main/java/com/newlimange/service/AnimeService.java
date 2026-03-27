package com.newlimange.service;

import com.newlimange.dto.AnimeDTO;
import com.newlimange.dto.StatsDTO;
import com.newlimange.entity.Anime;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 动漫业务逻辑接口
 * by shengjing19
 * created 2026-01-03
 */
public interface AnimeService {

    /**
     * 添加动漫
     * @param animeDTO 表单数据
     * @param coverFile 封面图片文件
     */
    void addAnime(AnimeDTO animeDTO, MultipartFile coverFile) throws IOException;

    /**
     * 更新动漫
     * @param animeDTO 表单数据(包含ID)
     * @param coverFile 新的封面图片 (可选，如果为空则不更新图片)
     */
    void updateAnime(AnimeDTO animeDTO, MultipartFile coverFile) throws IOException;

    /**
     * 删除动漫
     * @param id 动漫ID
     */
    void deleteAnime(Integer id);

    /**
     * 根据ID获取详情
     * @param id 动漫ID
     * @return 动漫实体
     */
    Anime getAnimeById(Integer id);

    /**
     * 分页获取列表
     * @param type 类型 (favorite, watching, all)
     * @param page 当前页码 (从1开始)
     * @param size 每页数量
     * @return 分页结果
     */
    Page<Anime> getAnimeList(String type, int page, int size);

    /**
     * 获取"已看完"的统计分组数据
     * 逻辑最复杂的部分：按年-月分组
     * @return 符合前端 JSON 结构的 Map
     */
    Map<String, Object> getFinishedAnimesGrouped();

    /**
     * 获取"最喜欢"的列表
     */
    Map<String, Object> getFavoriteAnimes();

    /**
     * 获取"正在追"的列表
     */
    Map<String, Object> getWatchingAnimes();

    /**
     * 获取统计数据
     */
    Map<String, Object> getStats();

    StatsDTO getWeeklyStats();
    StatsDTO getAnnualStats();
}