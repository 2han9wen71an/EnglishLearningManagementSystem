package com.chun.myspringboot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DeepSeekService {

    @Value("${deepseek.api.key:your-api-key}")
    private String apiKey;

    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送对话请求到DeepSeek API
     * @param scenarioDescription 场景描述
     * @param messages 对话历史
     * @param userPrompt 用户提示
     * @return AI回复
     */
    public String generateResponse(String scenarioDescription, List<String> messages, String userPrompt) {
        try {
            // 检查API密钥是否为默认值
            if ("your-api-key".equals(apiKey)) {
                // 如果是默认API密钥，返回模拟回复
                return generateMockResponse(scenarioDescription, userPrompt);
            }

            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            // 构建消息数组
            ArrayNode messagesArray = requestBody.putArray("messages");

            // 系统消息 - 设置角色和场景
            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a helpful English conversation partner in the following scenario: " + scenarioDescription +
                    ". Respond naturally as if you are in this scenario. Keep responses concise and appropriate for the context.");
            messagesArray.add(systemMessage);

            // 添加历史对话
            for (int i = 0; i < messages.size(); i++) {
                ObjectNode message = objectMapper.createObjectNode();
                message.put("role", i % 2 == 0 ? "assistant" : "user");
                message.put("content", messages.get(i));
                messagesArray.add(message);
            }

            // 添加用户当前输入
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);
            messagesArray.add(userMessage);

            // 构建HTTP请求
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            // 使用兼容的方式创建RequestBody
            RequestBody body = new RequestBody() {
                @Override
                public MediaType contentType() {
                    return JSON;
                }

                @Override
                public void writeTo(okio.BufferedSink sink) throws IOException {
                    sink.writeUtf8(jsonBody);
                }
            };

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // 发送请求并获取响应
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                String responseBody = response.body().string();
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                // 解析响应获取AI回复
                String aiResponse = jsonResponse.path("choices").path(0).path("message").path("content").asText();
                return aiResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 出错时返回模拟回复
            return generateMockResponse(scenarioDescription, userPrompt);
        }
    }

    /**
     * 生成模拟回复（当API不可用时使用）
     */
    private String generateMockResponse(String scenarioDescription, String userPrompt) {
        // 根据场景和用户输入生成简单的模拟回复
        if (scenarioDescription.contains("机场")) {
            return "I understand you need assistance with your flight. Let me help you with the check-in process. Could you please show me your passport and booking confirmation?";
        } else if (scenarioDescription.contains("酒店")) {
            return "Welcome to our hotel! I'd be happy to assist with your reservation. We have several room types available. Would you prefer a standard room or a suite?";
        } else if (scenarioDescription.contains("餐厅")) {
            return "Thank you for your order. Our chef will prepare it right away. Would you like to order any drinks while you wait?";
        } else if (scenarioDescription.contains("购物")) {
            return "We have that item in stock. Would you like to try it on? We also have it in different colors if you're interested.";
        } else if (scenarioDescription.contains("医院")) {
            return "I understand your symptoms. Based on what you've told me, I recommend we run some tests to get a better understanding of your condition.";
        } else {
            return "I understand what you're saying. Could you please tell me more about what you need help with?";
        }
    }

    /**
     * 生成情景对话的下一个回复
     * @param scenarioName 场景名称
     * @param scenarioDescription 场景描述
     * @param dialogHistory 对话历史
     * @param userInput 用户输入
     * @return AI回复
     */
    public String generateDialogueResponse(String scenarioName, String scenarioDescription,
                                          List<String> dialogHistory, String userInput) {
        // 构建完整的场景描述
        String fullScenarioDescription = scenarioName + ": " + scenarioDescription;

        // 调用API生成回复
        return generateResponse(fullScenarioDescription, dialogHistory, userInput);
    }
}
