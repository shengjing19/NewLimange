package com.newlimange.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 * 配置静态资源处理，建立"虚拟路径"映射。
 * 由于 Spring Boot 打包运行后无法直接访问外部磁盘文件，
 * 此配置将 URL 路径 "/uploads/**" 映射到本地物理路径 例"D:/liamge/uploads/**"，
 * 从而实现浏览器直接访问服务器本地存储的上传图片。
 * by shengjing19(Hisx12123)
 * created 2025/12/27
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 如果 uploadPath 是 "./uploads"，转换为绝对路径为 file:./uploads/
        String pathInfo = "file:" + uploadPath + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(pathInfo);
    }
}