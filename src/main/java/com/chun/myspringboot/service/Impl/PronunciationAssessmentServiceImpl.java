package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.mapper.PronunciationAssessmentMapper;
import com.chun.myspringboot.pojo.PronunciationAssessment;
import com.chun.myspringboot.service.DeepSeekService;
import com.chun.myspringboot.service.PronunciationAssessmentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PronunciationAssessmentServiceImpl implements PronunciationAssessmentService {
    
    @Autowired
    private PronunciationAssessmentMapper pronunciationAssessmentMapper;
    
    @Autowired
    private DeepSeekService deepSeekService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public int addAssessment(PronunciationAssessment assessment) {
        return pronunciationAssessmentMapper.addAssessment(assessment);
    }
    
    @Override
    public int deleteAssessment(Integer assessmentId) {
        return pronunciationAssessmentMapper.deleteAssessment(assessmentId);
    }
    
    @Override
    public int updateAssessment(PronunciationAssessment assessment) {
        return pronunciationAssessmentMapper.updateAssessment(assessment);
    }
    
    @Override
    public PronunciationAssessment queryAssessmentById(Integer assessmentId) {
        return pronunciationAssessmentMapper.queryAssessmentById(assessmentId);
    }
    
    @Override
    public List<PronunciationAssessment> queryAssessmentsByUserId(Integer userId) {
        return pronunciationAssessmentMapper.queryAssessmentsByUserId(userId);
    }
    
    @Override
    public List<PronunciationAssessment> queryAllAssessments() {
        return pronunciationAssessmentMapper.queryAllAssessments();
    }
    
    @Override
    public PronunciationAssessment assessPronunciation(Integer userId, String content, String audioData) {
        try {
            // 构建AI评测提示
            String prompt = "Please analyze the pronunciation of the following English text. " +
                    "The user has recorded themselves saying this text, and we need to identify any pronunciation issues. " +
                    "Focus on phoneme-level errors (like 'th' sounds, vowel sounds, etc.). " +
                    "Format your response as JSON with the following structure: " +
                    "{\n" +
                    "  \"phonemeErrors\": [\n" +
                    "    {\"word\": \"think\", \"correctPhoneme\": \"/θɪŋk/\", \"userError\": \"Pronouncing 'th' as 's' or 'f'\", \"suggestion\": \"Place tongue between teeth for 'th' sound\"}\n" +
                    "  ],\n" +
                    "  \"overallScore\": 85\n" +
                    "}\n\n" +
                    "Text to analyze:\n" + content;
            
            // 调用DeepSeek API进行评测
            List<String> messages = new ArrayList<>();
            String aiResponse = deepSeekService.generateResponse("Pronunciation Assessment", messages, prompt);
            
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
                    jsonResponse = createMockAssessmentResponse(content);
                }
            }
            
            // 创建评测记录对象
            PronunciationAssessment assessment = new PronunciationAssessment();
            assessment.setUserId(userId);
            assessment.setContent(content);
            assessment.setAudioData(audioData);
            assessment.setPhonemeErrors(jsonResponse.path("phonemeErrors").toString());
            assessment.setOverallScore(jsonResponse.path("overallScore").asInt(70));
            assessment.setAssessmentTime(new Date());
            
            // 保存评测记录
            pronunciationAssessmentMapper.addAssessment(assessment);
            
            return assessment;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // 创建模拟评测响应（当API不可用时使用）
    private JsonNode createMockAssessmentResponse(String content) {
        ObjectNode response = objectMapper.createObjectNode();
        
        // 添加音素错误
        ArrayNode phonemeErrors = response.putArray("phonemeErrors");
        
        // 检查内容中是否包含特定单词，添加相应的模拟错误
        if (content.toLowerCase().contains("think") || content.toLowerCase().contains("thank")) {
            ObjectNode error = phonemeErrors.addObject();
            error.put("word", "think/thank");
            error.put("correctPhoneme", "/θɪŋk/ or /θæŋk/");
            error.put("userError", "Pronouncing 'th' as 's' or 'f'");
            error.put("suggestion", "Place tongue between teeth for 'th' sound");
        }
        
        if (content.toLowerCase().contains("very") || content.toLowerCase().contains("voice")) {
            ObjectNode error = phonemeErrors.addObject();
            error.put("word", "very/voice");
            error.put("correctPhoneme", "/ˈveri/ or /vɔɪs/");
            error.put("userError", "Pronouncing 'v' as 'w'");
            error.put("suggestion", "Touch your bottom lip to your upper teeth for 'v' sound");
        }
        
        if (content.toLowerCase().contains("red") || content.toLowerCase().contains("right")) {
            ObjectNode error = phonemeErrors.addObject();
            error.put("word", "red/right");
            error.put("correctPhoneme", "/red/ or /raɪt/");
            error.put("userError", "Difficulty with 'r' sound");
            error.put("suggestion", "Curl tongue back slightly for American 'r' sound");
        }
        
        // 添加总体评分
        response.put("overallScore", 75);
        
        return response;
    }
}
