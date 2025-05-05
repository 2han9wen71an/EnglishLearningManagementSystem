package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.pojo.WordCard;
import com.chun.myspringboot.service.Impl.WordCardServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 单词卡片API控制器
 */
@RestController
@RequestMapping("/api/wordcards")
public class WordCardApiController {

    @Autowired
    private WordCardServiceImpl wordCardService;

    /**
     * 生成单词卡片
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<WordCard>> generateWordCard(@RequestParam String word, @RequestParam(required = false) Integer userId) {
        try {
            // 生成单词卡片
            WordCard wordCard = wordCardService.generateWordCard(word);

            // 如果提供了用户ID，关联用户
            if (userId != null && wordCard.getUserId() == null) {
                wordCard.setUserId(userId);
                wordCardService.saveWordCard(wordCard);
            }

            return ResponseEntity.ok(ApiResponse.success("单词卡片生成成功", wordCard));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("生成单词卡片失败：" + e.getMessage()));
        }
    }

    /**
     * 获取用户的单词卡片历史
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<WordCard>>> getUserWordCards(@PathVariable Integer userId) {
        List<WordCard> wordCards = wordCardService.queryWordCardsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(wordCards));
    }

    /**
     * 获取单词卡片详情
     */
    @GetMapping("/word/{word}")
    public ResponseEntity<ApiResponse<WordCard>> getWordCardByWord(@PathVariable String word) {
        WordCard wordCard = wordCardService.queryWordCardByWord(word);
        if (wordCard != null) {
            return ResponseEntity.ok(ApiResponse.success(wordCard));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("单词卡片不存在"));
        }
    }

    /**
     * 保存单词卡片
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WordCard>> saveWordCard(@RequestBody WordCard wordCard) {
        try {
            wordCardService.saveWordCard(wordCard);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("单词卡片保存成功", wordCard));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("保存单词卡片失败：" + e.getMessage()));
        }
    }

    /**
     * 删除单词卡片
     */
    @DeleteMapping("/word/{word}")
    public ResponseEntity<ApiResponse<String>> deleteWordCard(@PathVariable String word) {
        // 检查单词卡片是否存在
        WordCard existingCard = wordCardService.queryWordCardByWord(word);
        if (existingCard == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("单词卡片不存在"));
        }

        // 由于没有直接的删除方法，我们可以返回一个提示信息
        return ResponseEntity.ok(ApiResponse.success("该功能暂未实现，请联系管理员", null));
    }
}
