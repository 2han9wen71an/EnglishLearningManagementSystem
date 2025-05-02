package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.service.ImageGenerationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class ImageGenerationServiceImpl implements ImageGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(ImageGenerationServiceImpl.class);

    @Value("${deepseek.api.key:your-api-key}")
    private String deepseekApiKey;

    @Value("${siliconflow.api.key:your-siliconflow-api-key}")
    private String siliconflowApiKey;

    @Value("${siliconflow.api.url:https://api.siliconflow.cn/v1/images/generations}")
    private String siliconflowApiUrl;

    @Value("${siliconflow.api.model:black-forest-labs/FLUX.1-schnell}")
    private String siliconflowModel;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateImage(String word) {
        try {
            // 检查硅基流动API密钥是否为默认值
            if ("your-siliconflow-api-key".equals(siliconflowApiKey)) {
                logger.info("使用默认硅基流动API密钥，返回占位图像URL");
                return getPlaceholderImageUrl(word);
            }

            // 使用FLUX.1-schnell API生成图像
            return generateImageWithFlux(word);
        } catch (Exception e) {
            logger.error("生成图像时发生错误", e);
            // 出错时返回占位图像URL
            return getPlaceholderImageUrl(word);
        }
    }

    /**
     * 使用Black Forest Labs的FLUX.1-schnell API生成图像
     * @param word 单词
     * @return 图像URL
     */
    private String generateImageWithFlux(String word) {
        try {
            logger.info("开始使用{}模型生成图像，单词: {}", siliconflowModel, word);

            // 构建提示词，使其更具描述性，并避免在图像中显示文字
            String prompt = "A high-quality, detailed image representing the concept of '" + word + "'. " +
                    "The image should be clear, visually appealing, and accurately depict the concept without any text or words in the image. " +
                    "Do not include any text, letters, or words in the generated image.";

            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", siliconflowModel);
            requestBody.put("prompt", prompt);
            requestBody.put("negative_prompt", "text, words, letters, writing, labels, watermark, signature, logo");
            requestBody.put("width", 768); // 设置图像宽度
            requestBody.put("height", 512); // 设置图像高度
            requestBody.put("seed", (int)(Math.random() * 1000000)); // 随机种子以获得不同结果

            // 构建HTTP请求
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            RequestBody body = RequestBody.create(jsonBody, JSON);

            Request request = new Request.Builder()
                    .url(siliconflowApiUrl)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + siliconflowApiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // 发送请求并获取响应
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("API请求失败，状态码: {}", response.code());
                    throw new IOException("API请求失败，状态码: " + response.code());
                }

                String responseBody = response.body().string();
                logger.debug("API响应: {}", responseBody);
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                // 解析响应获取图像URL
                if (jsonResponse.has("data") && jsonResponse.get("data").isArray() && jsonResponse.get("data").size() > 0) {
                    JsonNode dataNode = jsonResponse.get("data").get(0);

                    // 检查是否有URL字段
                    if (dataNode.has("url")) {
                        String imageUrl = dataNode.get("url").asText();
                        logger.info("成功获取图像URL: {}", imageUrl);
                        return imageUrl;
                    }

                    // 检查是否有base64字段
                    if (dataNode.has("b64_json")) {
                        // 这里可以处理base64编码的图像数据
                        // 在实际应用中，你可能需要将base64数据保存为图像文件并返回URL
                        logger.info("获取到base64编码的图像数据");
                        // 这里简化处理，返回占位图像
                        return getPlaceholderImageUrl(word);
                    }
                }

                logger.warn("无法从API响应中获取图像URL，返回占位图像");
                return getPlaceholderImageUrl(word);
            }
        } catch (Exception e) {
            logger.error("调用{}模型API时发生错误", siliconflowModel, e);
            return getPlaceholderImageUrl(word);
        }
    }

    /**
     * 获取占位图像URL
     * 根据单词生成一个占位图像URL
     */
    private String getPlaceholderImageUrl(String word) {
        // 使用占位图像服务
        String url = "https://via.placeholder.com/500x300?text=" + word;
        logger.info("返回占位图像URL: {}", url);
        return url;
    }
}
