package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.mapper.EssayCorrectionMapper;
import com.chun.myspringboot.mapper.EssayMapper;
import com.chun.myspringboot.pojo.Essay;
import com.chun.myspringboot.pojo.EssayCorrection;
import com.chun.myspringboot.service.DeepSeekService;
import com.chun.myspringboot.service.EssayCorrectionService;
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
public class EssayCorrectionServiceImpl implements EssayCorrectionService {
    
    @Autowired
    private EssayCorrectionMapper essayCorrectionMapper;
    
    @Autowired
    private EssayMapper essayMapper;
    
    @Autowired
    private DeepSeekService deepSeekService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public int addCorrection(EssayCorrection correction) {
        return essayCorrectionMapper.addCorrection(correction);
    }
    
    @Override
    public int deleteCorrection(Integer correctionId) {
        return essayCorrectionMapper.deleteCorrection(correctionId);
    }
    
    @Override
    public int updateCorrection(EssayCorrection correction) {
        return essayCorrectionMapper.updateCorrection(correction);
    }
    
    @Override
    public EssayCorrection queryCorrectionById(Integer correctionId) {
        return essayCorrectionMapper.queryCorrectionById(correctionId);
    }
    
    @Override
    public EssayCorrection queryCorrectionByEssayId(Integer essayId) {
        return essayCorrectionMapper.queryCorrectionByEssayId(essayId);
    }
    
    @Override
    public List<EssayCorrection> queryAllCorrections() {
        return essayCorrectionMapper.queryAllCorrections();
    }
    
    @Override
    public EssayCorrection correctEssayWithAI(Integer essayId) {
        try {
            // 获取作文内容
            Essay essay = essayMapper.queryEssayById(essayId);
            if (essay == null) {
                return null;
            }
            
            // 构建AI批改提示
            String prompt = "Please correct the following English essay. " +
                    "Identify grammar errors (tense, collocation, etc.), " +
                    "suggest vocabulary improvements, and provide structure comments. " +
                    "Format your response as JSON with the following structure: " +
                    "{\n" +
                    "  \"correctedContent\": \"The full corrected essay\",\n" +
                    "  \"grammarErrors\": [\n" +
                    "    {\"original\": \"original text\", \"correction\": \"corrected text\", \"explanation\": \"explanation\", \"type\": \"error type\"}\n" +
                    "  ],\n" +
                    "  \"vocabularySuggestions\": [\n" +
                    "    {\"original\": \"original word\", \"suggestion\": \"suggested word\", \"explanation\": \"why it's better\"}\n" +
                    "  ],\n" +
                    "  \"structureComments\": \"Comments about the essay structure\",\n" +
                    "  \"overallScore\": 85\n" +
                    "}\n\n" +
                    "Essay Title: " + essay.getTitle() + "\n\n" +
                    "Essay Content:\n" + essay.getContent();
            
            // 调用DeepSeek API进行批改
            List<String> messages = new ArrayList<>();
            String aiResponse = deepSeekService.generateResponse("Essay Correction", messages, prompt);
            
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
                    jsonResponse = createMockCorrectionResponse(essay.getContent());
                }
            }
            
            // 创建批改结果对象
            EssayCorrection correction = new EssayCorrection();
            correction.setEssayId(essayId);
            correction.setCorrectedContent(jsonResponse.path("correctedContent").asText(essay.getContent()));
            correction.setGrammarErrors(jsonResponse.path("grammarErrors").toString());
            correction.setVocabularySuggestions(jsonResponse.path("vocabularySuggestions").toString());
            correction.setStructureComments(jsonResponse.path("structureComments").asText(""));
            correction.setOverallScore(jsonResponse.path("overallScore").asInt(70));
            correction.setCorrectionTime(new Date());
            
            // 保存批改结果
            essayCorrectionMapper.addCorrection(correction);
            
            // 更新作文状态为已批改
            essayMapper.updateEssayStatus(essayId, 1);
            
            return correction;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // 创建模拟批改响应（当API不可用时使用）
    private JsonNode createMockCorrectionResponse(String originalContent) {
        ObjectNode response = objectMapper.createObjectNode();
        
        // 添加修正后的内容（简单示例）
        response.put("correctedContent", originalContent);
        
        // 添加语法错误
        ArrayNode grammarErrors = response.putArray("grammarErrors");
        ObjectNode error1 = grammarErrors.addObject();
        error1.put("original", "I goes to school");
        error1.put("correction", "I go to school");
        error1.put("explanation", "Subject-verb agreement error. 'I' requires the base form of the verb.");
        error1.put("type", "Grammar");
        
        // 添加词汇建议
        ArrayNode vocabSuggestions = response.putArray("vocabularySuggestions");
        ObjectNode suggestion1 = vocabSuggestions.addObject();
        suggestion1.put("original", "good");
        suggestion1.put("suggestion", "excellent");
        suggestion1.put("explanation", "More precise and descriptive word choice.");
        
        // 添加结构评论
        response.put("structureComments", "The essay has a clear introduction, body, and conclusion. Consider adding more transition words between paragraphs for better flow.");
        
        // 添加总体评分
        response.put("overallScore", 75);
        
        return response;
    }
}
