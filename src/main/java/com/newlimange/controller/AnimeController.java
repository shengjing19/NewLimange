package com.newlimange.controller;

import com.newlimange.dto.AnimeDTO;
import com.newlimange.dto.StatsDTO;
import com.newlimange.service.AnimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * limange主控制器
 * by shengjing19(Hisx12123)
 * created 2026-01-03
 */
@RestController
@RequestMapping("/api/anime")
public class AnimeController {

    private final AnimeService animeService;

    public AnimeController(AnimeService animeService) {
        this.animeService = animeService;
    }

    /**
     * 添加动漫（数据管理）
     */
    @PostMapping("/add")
    public ResponseEntity<?> addAnime(
            @ModelAttribute AnimeDTO animeDTO, // 接收表单字段
            @RequestParam("coverImage") MultipartFile file
    ) {
        try {
            animeService.addAnime(animeDTO, file);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("上传失败: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    /**
     * 修改动漫信息（数据管理）
     */
    @PostMapping("/update")
    public ResponseEntity<?> updateAnime(
            @ModelAttribute AnimeDTO animeDTO,
            @RequestParam(value = "coverImage", required = false) MultipartFile file
    ) {
        try {
            animeService.updateAnime(animeDTO, file);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("更新失败: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    /**
     * 动漫信息分页显示（数据管理）
     */
    @GetMapping("/list")
    public ResponseEntity<?> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String type
    ) {
        // 用 getAnimeList，传入 page 和固定大小 5。Service 内部会处理 page - 1 的逻辑
        return ResponseEntity.ok(animeService.getAnimeList(type, page, 5));
    }

    /**
     * 删除对应动漫记录（数据管理）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnime(@PathVariable Integer id) {
        animeService.deleteAnime(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 动漫记录主服务
     * 获取对应的动漫信息
     */
    @GetMapping("/main")
    public ResponseEntity<?> getMainData(@RequestParam String type) {
        Object result;
        switch (type) {
            case "finished":
                result = animeService.getFinishedAnimesGrouped();
                break;
            case "favorite":
                result = animeService.getFavoriteAnimes();
                break;
            case "watching":
                result = animeService.getWatchingAnimes();
                break;
            case "stats":
                result = animeService.getStats();
                break;
            default:
                return ResponseEntity.badRequest().body("Invalid type");
        }
        return ResponseEntity.ok(result);
    }


    /**
     * 动漫详细信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(animeService.getAnimeById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * 状态-每周观看情况
     */
    @GetMapping("/stats/weekly")
    public ResponseEntity<StatsDTO> getWeeklyStats() {
        return ResponseEntity.ok(animeService.getWeeklyStats());
    }
    /**
     * 状态-年度观看情况
     */
    @GetMapping("/stats/annual")
    public ResponseEntity<StatsDTO> getAnnualStats() {
        return ResponseEntity.ok(animeService.getAnnualStats());
    }
}