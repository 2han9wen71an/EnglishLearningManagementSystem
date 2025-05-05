package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.pojo.PronunciationAssessment;
import com.chun.myspringboot.service.Impl.PronunciationAssessmentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 发音评测API控制器
 */
@RestController
@RequestMapping("/api/pronunciations")
public class PronunciationApiController {
    
    @Autowired
    private PronunciationAssessmentServiceImpl pronunciationAssessmentService;
    
    /**
     * 获取用户的所有评测记录
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PronunciationAssessment>>> getUserAssessments(@PathVariable Integer userId) {
        List<PronunciationAssessment> assessments = pronunciationAssessmentService.queryAssessmentsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(assessments));
    }
    
    /**
     * 获取评测详情
     */
    @GetMapping("/{assessmentId}")
    public ResponseEntity<ApiResponse<PronunciationAssessment>> getAssessmentById(@PathVariable Integer assessmentId) {
        PronunciationAssessment assessment = pronunciationAssessmentService.queryAssessmentById(assessmentId);
        if (assessment != null) {
            return ResponseEntity.ok(ApiResponse.success(assessment));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("评测记录不存在"));
        }
    }
    
    /**
     * 提交发音评测
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PronunciationAssessment>> submitPronunciation(
            @RequestParam Integer userId,
            @RequestParam String content,
            @RequestParam String audioData) {
        try {
            PronunciationAssessment assessment = pronunciationAssessmentService.assessPronunciation(
                    userId, content, audioData);
            
            if (assessment != null) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("评测成功", assessment));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("评测失败，请稍后重试"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("系统错误：" + e.getMessage()));
        }
    }
    
    /**
     * 删除评测记录
     */
    @DeleteMapping("/{assessmentId}")
    public ResponseEntity<ApiResponse<String>> deleteAssessment(@PathVariable Integer assessmentId) {
        // 检查评测记录是否存在
        PronunciationAssessment existingAssessment = pronunciationAssessmentService.queryAssessmentById(assessmentId);
        if (existingAssessment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("评测记录不存在"));
        }
        
        int result = pronunciationAssessmentService.deleteAssessment(assessmentId);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("评测记录删除成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除失败，请稍后重试"));
        }
    }
    
    /**
     * 管理员：获取所有评测记录
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PronunciationAssessment>>> getAllAssessments() {
        List<PronunciationAssessment> assessments = pronunciationAssessmentService.queryAllAssessments();
        return ResponseEntity.ok(ApiResponse.success(assessments));
    }
}
