package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.mapper.WordCardMapper;
import com.chun.myspringboot.pojo.WordCard;
import com.chun.myspringboot.service.DeepSeekService;
import com.chun.myspringboot.service.ImageGenerationService;
import com.chun.myspringboot.service.WordCardService;
import com.chun.myspringboot.service.WordService;
import com.chun.myspringboot.utils.MarkdownUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class WordCardServiceImpl implements WordCardService {

    @Autowired
    private WordCardMapper wordCardMapper;

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private ImageGenerationService imageGenerationService;

    @Autowired
    private WordService wordService;

    @Autowired
    private MarkdownUtils markdownUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public WordCard generateWordCard(String word) {
        try {
            // 首先检查是否已经存在该单词的卡片
            WordCard existingCard = queryWordCardByWord(word);
            if (existingCard != null) {
                return existingCard;
            }

            // 创建新的单词卡片
            WordCard wordCard = new WordCard();
            wordCard.setWord(word);
            wordCard.setCreateTime(new Date());

            // 获取单词解释
            String explanation = getWordExplanation(word);
            wordCard.setExplanation(explanation);

            // 生成场景例句和记忆口诀
            generateSentenceAndTip(wordCard);

            // 生成图片URL
            String imageUrl = imageGenerationService.generateImage(word);
            wordCard.setImageUrl(imageUrl);

            // 保存单词卡片
            saveWordCard(wordCard);

            return wordCard;
        } catch (Exception e) {
            e.printStackTrace();
            // 出错时返回基本卡片
            return createBasicWordCard(word);
        }
    }

    @Override
    public int saveWordCard(WordCard wordCard) {
        try {
            return wordCardMapper.addWordCard(wordCard);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public WordCard queryWordCardByWord(String word) {
        try {
            return wordCardMapper.queryWordCardByWord(word);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<WordCard> queryWordCardsByUserId(Integer userId) {
        try {
            return wordCardMapper.queryWordCardsByUserId(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 获取单词解释
     * 首先尝试从数据库中获取，如果没有则使用AI生成
     */
    private String getWordExplanation(String word) {
        try {
            // 尝试从数据库中获取单词解释
            com.chun.myspringboot.pojo.Word existingWord = wordService.queryWordByName(word);
            if (existingWord != null && existingWord.getExplanation() != null && !existingWord.getExplanation().isEmpty()) {
                return existingWord.getExplanation();
            }

            // 如果数据库中没有，使用DeepSeek API生成解释
            String prompt = "Please provide a concise explanation of the English word '" + word + "'. " +
                    "Include the part of speech, pronunciation, and definition. " +
                    "Format your response as a simple text explanation.";

            List<String> messages = new ArrayList<>();
            String aiResponse = deepSeekService.generateResponse("Word Explanation", messages, prompt);

            // 将Markdown格式的解释转换为HTML格式
            String htmlResponse = markdownUtils.markdownToHtml(aiResponse);

            return htmlResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return "No explanation available for '" + word + "'.";
        }
    }

    /**
     * 生成场景例句和记忆口诀
     */
    private void generateSentenceAndTip(WordCard wordCard) {
        try {
            String word = wordCard.getWord();

            // 构建提示
            String prompt = "Please generate the following for the English word '" + word + "':\n" +
                    "1. A contextual example sentence using the word naturally.\n" +
                    "2. A memory tip or mnemonic to help remember the word (like an acronym where each letter stands for something related to the word).\n\n" +
                    "Format your response as JSON with the following structure:\n" +
                    "{\n" +
                    "  \"contextSentence\": \"Your example sentence here.\",\n" +
                    "  \"memoryTip\": \"Your memory tip here.\"\n" +
                    "}";

            List<String> messages = new ArrayList<>();
            String aiResponse = deepSeekService.generateResponse("Word Card Generation", messages, prompt);

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
                    jsonResponse = createMockResponse(word);
                }
            }

            // 设置场景例句和记忆口诀
            wordCard.setContextSentence(jsonResponse.path("contextSentence").asText("No example sentence available."));
            wordCard.setMemoryTip(jsonResponse.path("memoryTip").asText("No memory tip available."));

        } catch (Exception e) {
            e.printStackTrace();
            // 出错时设置默认值
            wordCard.setContextSentence("No example sentence available.");
            wordCard.setMemoryTip("No memory tip available.");
        }
    }

    /**
     * 创建模拟响应
     */
    private JsonNode createMockResponse(String word) {
        try {
            ObjectNode response = objectMapper.createObjectNode();

            // 根据单词生成模拟场景例句和记忆口诀
            if (word.equalsIgnoreCase("apple")) {
                response.put("contextSentence", "I ate a crunchy red apple for breakfast.");
                response.put("memoryTip", "A=Amazing, P=Perfectly, P=Plump, L=Luscious, E=Edible");
            } else if (word.equalsIgnoreCase("book")) {
                response.put("contextSentence", "She borrowed a book from the library last week.");
                response.put("memoryTip", "B=Browse, O=Open, O=Observe, K=Knowledge");
            } else if (word.equalsIgnoreCase("computer")) {
                response.put("contextSentence", "My new computer has a fast processor and plenty of memory.");
                response.put("memoryTip", "C=Calculate, O=Organize, M=Manage, P=Process, U=Utilize, T=Technology, E=Efficiently, R=Rapidly");
            } else {
                response.put("contextSentence", "The word '" + word + "' is commonly used in everyday conversation.");
                response.put("memoryTip", "Try creating an acronym where each letter of '" + word + "' stands for something related to its meaning.");
            }

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return objectMapper.createObjectNode();
        }
    }

    /**
     * 创建基本单词卡片
     * 当出现错误时使用
     */
    private WordCard createBasicWordCard(String word) {
        WordCard wordCard = new WordCard();
        wordCard.setWord(word);
        wordCard.setExplanation("No explanation available.");
        wordCard.setContextSentence("No example sentence available.");
        wordCard.setMemoryTip("No memory tip available.");
        wordCard.setImageUrl("https://via.placeholder.com/300x200?text=" + word);
        wordCard.setCreateTime(new Date());
        return wordCard;
    }
}
