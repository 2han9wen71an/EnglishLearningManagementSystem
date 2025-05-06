package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件API控制器
 * 处理文件上传和访问
 */
@RestController
@RequestMapping("/api/files")
public class FileApiController {

    // 不需要身份验证，移除 JwtTokenUtil

    /**
     * 获取音频文件
     * 公开访问，不需要身份验证
     */
    @GetMapping("/audio/{filename:.+}")
    public ResponseEntity<Resource> getAudio(@PathVariable String filename) {
        try {
            // 音频文件公开访问，不需要身份验证

            // 构建文件路径
            String path = "static/video/" + filename;
            Resource resource = new ClassPathResource(path);

            // 检查文件是否存在
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // 设置响应头
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 上传音频文件
     */
    @PostMapping("/upload/audio")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAudio(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("请选择要上传的文件"));
        }

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // 确保目录存在
            String uploadDir = "src/main/resources/static/video";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // 返回文件路径
            Map<String, String> result = new HashMap<>();
            result.put("path", filename);

            return ResponseEntity.ok(ApiResponse.success("文件上传成功", result));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("文件上传失败: " + e.getMessage()));
        }
    }
}
