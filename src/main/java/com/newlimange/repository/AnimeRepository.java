package com.newlimange.repository;

import com.newlimange.entity.Anime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * 动漫数据仓库(从数据库中获取或存储数据)
 * by shengjing19(Hisx12123)
 * created 2026-01-03
 */
public interface AnimeRepository extends JpaRepository<Anime, Integer> {
    /**
     * 查找有关最喜欢的动漫数据
     * */
    Page<Anime> findByIsFavoriteTrue(Pageable pageable);
    long countByIsFavoriteTrue();

    /**
     * 查找有关正在追的动漫数据
     * */
    Page<Anime> findByIsWatchingTrue(Pageable pageable);
    long countByIsWatchingTrue();
    long countByIsWatchingFalse();

    /**
     * 统计一周内内每天的完成观看的数量 (用于周统计)
     * */
    @Query("SELECT a.finishDate, COUNT(a) FROM Anime a " +
            "WHERE a.finishDate BETWEEN :startDate AND :endDate GROUP BY a.finishDate")
    List<Object[]> countByFinishDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计某一年每月的完成观看的数量 (用于年度统计)
     * */
    @Query("SELECT FUNCTION('MONTH', a.finishDate), COUNT(a) FROM Anime a " +
            "WHERE FUNCTION('YEAR', a.finishDate) = :year GROUP BY FUNCTION('MONTH', a.finishDate)")
    List<Object[]> countByYear(@Param("year") int year);

}