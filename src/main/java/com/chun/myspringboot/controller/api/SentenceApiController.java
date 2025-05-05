package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.pojo.Sentence;
import com.chun.myspringboot.service.Impl.SentenceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 每日一句API控制器
 */
@RestController
@RequestMapping("/api/sentences")
public class SentenceApiController {
    
    @Autowired
    private SentenceServiceImpl sentenceService;
    
    /**
     * 获取所有句子
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Sentence>>> getAllSentences() {
        List<Sentence> sentences = sentenceService.queryAllSentence();
        return ResponseEntity.ok(ApiResponse.success(sentences));
    }
    
    /**
     * 获取随机句子
     */
    @GetMapping("/random")
    public ResponseEntity<ApiResponse<Sentence>> getRandomSentence() {
        Sentence sentence = sentenceService.queryRandomSentence();
        if (sentence != null) {
            return ResponseEntity.ok(ApiResponse.success(sentence));
        } else {
            // 如果没有句子，创建一个默认句子
            Sentence defaultSentence = new Sentence();
            defaultSentence.setSentenceName("The best preparation for tomorrow is doing your best today.");
            defaultSentence.setExplain("对明天最好的准备就是今天做到最好。");
            
            return ResponseEntity.ok(ApiResponse.success(defaultSentence));
        }
    }
    
    /**
     * 获取句子详情
     */
    @GetMapping("/{sentenceId}")
    public ResponseEntity<ApiResponse<Sentence>> getSentenceById(@PathVariable Integer sentenceId) {
        Sentence sentence = sentenceService.querySentenceById(sentenceId);
        if (sentence != null) {
            return ResponseEntity.ok(ApiResponse.success(sentence));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("句子不存在"));
        }
    }
    
    /**
     * 管理员：添加句子
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Sentence>> addSentence(@RequestBody Sentence sentence) {
        int result = sentenceService.addSentence(sentence);
        if (result > 0) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("句子添加成功", sentence));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("添加失败，请稍后重试"));
        }
    }
    
    /**
     * 管理员：更新句子
     */
    @PutMapping("/{sentenceId}")
    public ResponseEntity<ApiResponse<String>> updateSentence(@PathVariable Integer sentenceId, @RequestBody Sentence sentence) {
        // 确保sentenceId匹配
        if (!sentenceId.equals(sentence.getSentenceId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("句子ID不匹配"));
        }
        
        // 检查句子是否存在
        Sentence existingSentence = sentenceService.querySentenceById(sentenceId);
        if (existingSentence == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("句子不存在"));
        }
        
        int result = sentenceService.updateSentence(sentence);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("句子更新成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新失败，请稍后重试"));
        }
    }
    
    /**
     * 管理员：删除句子
     */
    @DeleteMapping("/{sentenceId}")
    public ResponseEntity<ApiResponse<String>> deleteSentence(@PathVariable Integer sentenceId) {
        // 检查句子是否存在
        Sentence existingSentence = sentenceService.querySentenceById(sentenceId);
        if (existingSentence == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("句子不存在"));
        }
        
        int result = sentenceService.deleteSentence(sentenceId);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("句子删除成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除失败，请稍后重试"));
        }
    }
}
