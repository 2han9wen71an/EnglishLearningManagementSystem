package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.service.AIEssayScoreService;
import com.chun.myspringboot.service.DeepSeekService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI简答题评分服务实现类
 */
@Service
public class AIEssayScoreServiceImpl implements AIEssayScoreService {

    @Autowired
    private DeepSeekService deepSeekService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public int scoreEssay(String userAnswer, String correctAnswer, int maxScore) {
        try {
            // 构建AI评分提示
            String prompt = "请评估以下英语简答题的答案质量。\n\n" +
                    "参考答案：\n" + correctAnswer + "\n\n" +
                    "学生答案：\n" + userAnswer + "\n\n" +
                    "请根据以下标准进行评分：\n" +
                    "1. 内容相关性（答案是否回答了问题）\n" +
                    "2. 准确性（答案中的事实和概念是否正确）\n" +
                    "3. 完整性（答案是否涵盖了所有要点）\n" +
                    "4. 语言表达（语法、词汇和句子结构是否正确）\n\n" +
                    "满分为" + maxScore + "分。请以JSON格式返回评分结果：\n" +
                    "{\n" +
                    "  \"score\": 分数（0-" + maxScore + "的整数）,\n" +
                    "  \"explanation\": \"评分解释\"\n" +
                    "}";
            
            // 调用DeepSeek API进行评分
            List<String> messages = new ArrayList<>();
            String aiResponse = deepSeekService.generateResponse("Essay Scoring", messages, prompt);
            
            // 解析AI响应
            JsonNode jsonResponse;
            try {
                // 尝试直接解析JSON
                jsonResponse = objectMapper.readTree(aiResponse);
            } catch (Exception e) {
                // 如果直接解析失败，尝试提取JSON部分
                int startIndex = aiResponse.indexOf("{");
                int endIndex = aiResponse.lastIndexOf("}") + 1;
                if (startIndex >= 0 && endIndex > startIndex) {
                    String jsonPart = aiResponse.substring(startIndex, endIndex);
                    jsonResponse = objectMapper.readTree(jsonPart);
                } else {
                    // 如果无法提取JSON，创建模拟响应
                    jsonResponse = createMockScoringResponse(userAnswer, correctAnswer, maxScore);
                }
            }
            
            // 获取分数
            int score = jsonResponse.path("score").asInt(0);
            
            // 确保分数在有效范围内
            if (score < 0) score = 0;
            if (score > maxScore) score = maxScore;
            
            return score;
        } catch (Exception e) {
            e.printStackTrace();
            // 出错时返回0分
            return 0;
        }
    }
    
    @Override
    public String getScoreExplanation(String userAnswer, String correctAnswer, int score, int maxScore) {
        try {
            // 构建AI评分解释提示
            String prompt = "请解释为什么以下英语简答题的答案得分为" + score + "分（满分" + maxScore + "分）。\n\n" +
                    "参考答案：\n" + correctAnswer + "\n\n" +
                    "学生答案：\n" + userAnswer + "\n\n" +
                    "请从以下方面进行分析：\n" +
                    "1. 内容相关性（答案是否回答了问题）\n" +
                    "2. 准确性（答案中的事实和概念是否正确）\n" +
                    "3. 完整性（答案是否涵盖了所有要点）\n" +
                    "4. 语言表达（语法、词汇和句子结构是否正确）\n\n" +
                    "请直接给出解释，不需要JSON格式。";
            
            // 调用DeepSeek API获取解释
            List<String> messages = new ArrayList<>();
            String explanation = deepSeekService.generateResponse("Essay Scoring Explanation", messages, prompt);
            
            return explanation;
        } catch (Exception e) {
            e.printStackTrace();
            return "无法获取评分解释。";
        }
    }
    
    // 创建模拟评分响应（当API不可用时使用）
    private JsonNode createMockScoringResponse(String userAnswer, String correctAnswer, int maxScore) {
        ObjectNode response = objectMapper.createObjectNode();
        
        // 简单比较用户答案和参考答案的相似度
        int score = calculateSimilarityScore(userAnswer, correctAnswer, maxScore);
        
        response.put("score", score);
        response.put("explanation", "这是一个模拟评分，基于答案的长度和关键词匹配。");
        
        return response;
    }
    
    // 简单的相似度评分算法
    private int calculateSimilarityScore(String userAnswer, String correctAnswer, int maxScore) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return 0;
        }
        
        if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
            return 0;
        }
        
        // 将答案转换为小写并分割为单词
        String[] userWords = userAnswer.toLowerCase().split("\\W+");
        String[] correctWords = correctAnswer.toLowerCase().split("\\W+");
        
        // 计算共同单词数
        int commonWords = 0;
        for (String userWord : userWords) {
            for (String correctWord : correctWords) {
                if (userWord.equals(correctWord)) {
                    commonWords++;
                    break;
                }
            }
        }
        
        // 计算相似度分数
        double similarity = (double) commonWords / correctWords.length;
        
        // 转换为分数
        int score = (int) Math.round(similarity * maxScore);
        
        // 确保分数在有效范围内
        if (score < 0) score = 0;
        if (score > maxScore) score = maxScore;
        
        return score;
    }
}
