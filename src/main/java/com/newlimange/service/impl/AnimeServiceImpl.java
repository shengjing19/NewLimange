package com.newlimange.service.impl;

import com.newlimange.dto.AnimeDTO;
import com.newlimange.dto.StatsDTO;
import com.newlimange.entity.Anime;
import com.newlimange.repository.AnimeRepository;
import com.newlimange.service.AnimeService;
import com.newlimange.utils.FileCheckUtil; // 假设你已将 PicUploadCheck 移植为此工具类
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 动漫业务逻辑具体实现
 * 负责处理所有与动漫数据相关的核心业务逻辑
 * by shengjing19
 * created 2026-01-03
 */
@Service
public class AnimeServiceImpl implements AnimeService {

    private final AnimeRepository animeRepository;

    // 从 application.yml 读取上传路径
    @Value("${app.upload-path:./uploads}")
    private String uploadPath;

    /**
     * 构造函数注入 Repository 依赖
     * @param animeRepository 动漫数据访问层组件
     */
    public AnimeServiceImpl(AnimeRepository animeRepository) {
        this.animeRepository = animeRepository;
    }

    /**
     * 添加新的动漫记录
     * 包含文件上传处理和实体数据转换保存。
     * * @param dto  前端传来的动漫数据传输对象
     * @param file 用户上传的动漫封面图片文件
     * @throws IOException 当文件保存失败时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAnime(AnimeDTO dto, MultipartFile file) throws IOException {
        String coverPath = saveFile(file);

        Anime anime = new Anime();
        BeanUtils.copyProperties(dto, anime);      // 将 DTO 属性复制到 Entity
        anime.setTotalEpisodes(dto.getEpisodes());// 手动处理 DTO 和 Entity 字段名不一致的情况 (episodes -> totalEpisodes)
        anime.setCoverImage(coverPath);

        animeRepository.save(anime);
    }

    /**
     * 更新现有的动漫记录
     * 如果传入了新文件，会保存新文件作为新封面。
     * * @param dto  包含修改后信息的动漫数据传输对象（必须包含有效 ID）
     * @param file 用户上传的新封面图片文件（可为空）
     * @throws IOException 当新文件保存失败时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAnime(AnimeDTO dto, MultipartFile file) throws IOException {
        Anime anime = animeRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("动漫不存在 ID: " + dto.getId()));

        // 更新基本信息
        anime.setTitle(dto.getTitle());
        anime.setTotalEpisodes(dto.getEpisodes());
        anime.setFinishDate(dto.getFinishDate());
        anime.setIsFavorite(dto.getIsFavorite());
        anime.setIsWatching(dto.getIsWatching());
        anime.setDescription(dto.getDescription());

        // 如果上传了新文件，则处理新文件并删除旧文件
        if (file != null && !file.isEmpty()) {
            deleteLocalFile(anime.getCoverImage()); // 删除旧图片文件
            String newCoverPath = saveFile(file);
            anime.setCoverImage(newCoverPath);
        }

        animeRepository.save(anime);
    }

    /**
     * 根据 ID 删除指定的动漫记录
     * 会同步删除服务器本地存储的封面图片文件。
     * * @param id 要删除的动漫的唯一标识 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnime(Integer id) {
        Anime anime = animeRepository.findById(id).orElse(null);
        if (anime != null) {
            deleteLocalFile(anime.getCoverImage()); // 删除本地文件
            animeRepository.delete(anime); // 删除数据库记录
        }
    }

    /**
     * 根据 ID 获取单部动漫的详细信息
     * * @param id 动漫的唯一标识 ID
     * @return 查找到的动漫实体对象
     * @throws RuntimeException 如果未找到对应 ID 的动漫则抛出异常
     */
    @Override
    public Anime getAnimeById(Integer id) {
        return animeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("未找到该动漫"));
    }

    /**
     * 分页获取动漫列表
     * 支持根据不同类型（最爱、在看、全部）进行条件过滤和分页查询。
     * * @param type 过滤类型 ("favorite" 为最爱, "watching" 为正在追, 其他为全部)
     * @param page 请求的页码（前端从 1 开始计算）
     * @param size 每页显示的数据条数
     * @return 包含当前页数据及总数信息的分页对象 (Page)
     */
    @Override
    public Page<Anime> getAnimeList(String type, int page, int size) {
        // Spring Data JPA 分页从 0 开始
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());

        if ("favorite".equals(type)) {
            return animeRepository.findByIsFavoriteTrue(pageable);
        } else if ("watching".equals(type)) {
            return animeRepository.findByIsWatchingTrue(pageable);
        } else {
            return animeRepository.findAll(pageable);
        }
    }

    /**
     * 获取已看完动漫列表，并按年份和月份进行分组
     * * @return 包含总数和按年月降序排列的分组动漫数据字典 (Map)
     */
    @Override
    public Map<String, Object> getFinishedAnimesGrouped() {
        // 获取所有"非正在追"(即已看完)的动漫
        List<Anime> finishedList = animeRepository.findAll().stream()
                .filter(a -> !a.getIsWatching())
                .collect(Collectors.toList());

        //  构造分组结构 Map<Year, Map<Month, List<Anime>>>
        // 使用 TreeMap 保持降序 (Collections.reverseOrder())
        Map<String, Map<String, List<Anime>>> groupedMap = new TreeMap<>(Collections.reverseOrder());

        for (Anime anime : finishedList) {
            if (anime.getFinishDate() == null) continue;

            String yearKey = anime.getFinishDate().getYear() + "年";
            // 格式化月份为 "05月"
            String monthKey = String.format("%02d月", anime.getFinishDate().getMonthValue());

            groupedMap
                    .computeIfAbsent(yearKey, k -> new TreeMap<>(Collections.reverseOrder()))
                    .computeIfAbsent(monthKey, k -> new ArrayList<>())
                    .add(anime);
        }

        // 转换为前端需要的 JSON 数组结构 List<Map<String, Object>>
        List<Map<String, Object>> yearGroups = new ArrayList<>();

        for (Map.Entry<String, Map<String, List<Anime>>> yearEntry : groupedMap.entrySet()) {
            for (Map.Entry<String, List<Anime>> monthEntry : yearEntry.getValue().entrySet()) {
                Map<String, Object> group = new HashMap<>();
                group.put("year", yearEntry.getKey());
                group.put("month", monthEntry.getKey());
                group.put("count", monthEntry.getValue().size());
                group.put("animes", monthEntry.getValue()); // 实体类会被自动序列化为 JSON
                yearGroups.add(group);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", finishedList.size());
        result.put("groupedAnimes", yearGroups);
        return result;
    }

    /**
     * 获取所有被标记为“最爱”的动漫列表（不分页）
     * * @return 包含最爱动漫总数和具体列表的字典 (Map)
     */
    @Override
    public Map<String, Object> getFavoriteAnimes() {
        // 这里不分页，获取全部
        List<Anime> list = animeRepository.findAll().stream()
                .filter(Anime::getIsFavorite)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("total", list.size());
        result.put("animes", list);
        return result;
    }

    /**
     * 获取所有当前标记为“正在追”的动漫列表（不分页）
     * * @return 包含正在追动漫总数和具体列表的字典 (Map)
     */
    @Override
    public Map<String, Object> getWatchingAnimes() {
        List<Anime> list = animeRepository.findAll().stream()
                .filter(Anime::getIsWatching)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("total", list.size());
        result.put("animes", list);
        return result;
    }

    /**
     * 获取动漫整体的简单统计数据
     * 包括：已看完总数、最爱总数、正在追总数。
     * * @return 包含各项总计数据的字典 (Map)
     */
    @Override
    public Map<String, Object> getStats() {
        List<Anime> all = animeRepository.findAll();
        long watching = all.stream().filter(Anime::getIsWatching).count();
        long favorite = all.stream().filter(Anime::getIsFavorite).count();
        long finished = all.stream().filter(a -> !a.getIsWatching()).count(); // 原逻辑，!isWatching 就是 finished

        Map<String, Object> result = new HashMap<>();
        result.put("totalFinished", finished);
        result.put("totalFavorite", favorite);
        result.put("totalWatching", watching);
        return result;
    }

    /**
     * 获取过去 7 天内每天看完的动漫数量统计数据
     * 专为前端图表（如 ECharts、Chart.js）渲染设计，自动补全没有数据的日期为 0。
     * * @return 包含日期标签(labels)和对应数值(values)的统计数据传输对象 (StatsDTO)
     */
    @Override
    public StatsDTO getWeeklyStats() {
        // 获取过去7天 (或者本周)
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6); // 包括今天共7天

        List<Object[]> results = animeRepository.countByFinishDateBetween(start, end);

        // 转为 Map 方便查找: <日期字符串, 数量>
        Map<String, Integer> dataMap = new HashMap<>();
        for (Object[] row : results) {
            dataMap.put(row[0].toString(), ((Number) row[1]).intValue());
        }

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");

        // 循环补全7天数据
        for (int i = 0; i < 7; i++) {
            LocalDate date = start.plusDays(i);
            String key = date.toString(); // 数据库返回通常是 yyyy-MM-dd

            labels.add(date.format(fmt)); // X轴显示 MM-dd
            values.add(dataMap.getOrDefault(key, 0)); // 如果没数据填0
        }

        return new StatsDTO(labels, values);
    }

    /**
     * 获取当前年份 1-12 月每月看完的动漫数量统计数据
     * 专为前端图表渲染设计，自动补全没有数据的月份为 0。
     * * @return 包含月份标签(labels)和对应数值(values)的统计数据传输对象 (StatsDTO)
     */
    @Override
    public StatsDTO getAnnualStats() {
        int currentYear = LocalDate.now().getYear();
        List<Object[]> results = animeRepository.countByYear(currentYear);

        Map<Integer, Integer> dataMap = new HashMap<>();
        for (Object[] row : results) {
            dataMap.put(((Number) row[0]).intValue(), ((Number) row[1]).intValue());
        }

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        // 循环补全12个月
        for (int i = 1; i <= 12; i++) {
            labels.add(i + "月");
            values.add(dataMap.getOrDefault(i, 0));
        }

        return new StatsDTO(labels, values);
    }

    /**
     * 统一处理图片文件的上传与安全检测逻辑
     * 会在文件名中加入时间戳防止重名，并调用工具类校验图片合法性。
     * * @param file 用户上传的原始图片文件
     * @return 保存成功后相对存储路径（用于存入数据库）
     * @throws IOException 如果文件传输或存储失败时抛出异常
     * @throws IllegalArgumentException 如果传入的文件为空
     * @throws RuntimeException 如果安全检测未通过或处理过程发生异常
     */
    private String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        // 确保目录存在
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String fullPath = uploadPath + File.separator + fileName;
        File dest = new File(fullPath);

        file.transferTo(dest);

        // 调用工具类进行安全检查
        try {
            if (!FileCheckUtil.checkPic(fullPath)) {
                dest.delete();
                throw new RuntimeException("文件安全检测未通过");
            }
        } catch (Exception e) {
            dest.delete(); // 确保异常时删除文件
            throw new RuntimeException("文件处理异常: " + e.getMessage());
        }

        // 返回相对路径，存入数据库
        return "uploads/" + fileName;
    }

    /**
     * 根据数据库中的相对路径删除本地存储的物理文件
     * 仅在后台静默执行，如果删除失败只会打印堆栈日志，不会阻断主业务流程（如删除记录）。
     * * @param relativePath 数据库中存储的文件相对路径（例如 "uploads/xxx.jpg"）
     */
    private void deleteLocalFile(String relativePath) {
        if (!StringUtils.hasText(relativePath)) return;
        try {
            //路径拼接
            String fileName = relativePath.replace("uploads/", "").replace("uploads\\", "");
            Path path = Paths.get(uploadPath, fileName);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace(); // 仅打印日志，不阻断主流程
        }
    }
}