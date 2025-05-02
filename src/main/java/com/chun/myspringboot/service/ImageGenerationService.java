package com.chun.myspringboot.service;

/**
 * 图像生成服务接口
 */
public interface ImageGenerationService {
    
    /**
     * 生成与单词相关的图像
     * @param word 单词
     * @return 图像URL或Base64编码的图像数据
     */
    String generateImage(String word);
}
