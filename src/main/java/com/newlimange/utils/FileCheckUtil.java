package com.newlimange.utils;

import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * 文件检测工具
 * 使用Apache Tika库
 * 检测文件内容和元数据是否符合要求
 * */
public class FileCheckUtil {

    private static final Tika TIKA = new Tika();
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif"
    );

    /**
     * 检查图片真实类型
     * @param file 接收到的 MultipartFile
     * @return true 验证通过
     */
    public static boolean checkPic(MultipartFile file) {
        try (InputStream stream = file.getInputStream()) {
            // Tika 会读取文件流的头部字节来判断真实类型，而不是相信扩展名
            String mimeType = TIKA.detect(stream);
            return ALLOWED_MIME_TYPES.contains(mimeType);
        } catch (IOException e) {
            return false;
        }
    }

    // 检查已经保存到磁盘的文件
    public static boolean checkPic(String filePath) {
        try {
            String mimeType = TIKA.detect(java.nio.file.Paths.get(filePath));
            return ALLOWED_MIME_TYPES.contains(mimeType);
        } catch (IOException e) {
            return false;
        }
    }
}