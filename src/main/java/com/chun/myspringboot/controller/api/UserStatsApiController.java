package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.mapper.UserWordMapper;
import com.chun.myspringboot.pojo.User;
import com.chun.myspringboot.service.ExamService;
import com.chun.myspringboot.service.ListenService;
import com.chun.myspringboot.service.UserService;
import com.chun.myspringboot.service.WordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户统计API控制器
 */
@Api(tags = "用户统计API")
@RestController
@RequestMapping("/api/users/{userId}/stats")
public class UserStatsApiController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private WordService wordService;
    
    @Autowired
    private ExamService examService;
    
    @Autowired
    private ListenService listenService;
    
    @Autowired
    private UserWordMapper userWordMapper;
    
    /**
     * 获取用户学习统计数据
     */
    @ApiOperation(value = "获取用户学习统计数据", notes = "获取用户的学习统计数据")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats(
            @ApiParam(value = "用户ID", required = true) @PathVariable Integer userId) {
        // 检查用户是否存在
        User user = userService.queryUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }
        
        Map<String, Object> stats = new HashMap<>();
        
        // 获取已学单词数量
        int wordsLearned = userWordMapper.queryUserLearnedWordsCount(userId);
        
        // 获取连续学习天数（这里需要实现一个方法来计算）
        int daysStreak = calculateDaysStreak(userId);
        
        // 获取考试次数
        int examsTaken = examService.queryUserExamCount(userId);
        
        stats.put("wordsLearned", wordsLearned);
        stats.put("daysStreak", daysStreak);
        stats.put("examsTaken", examsTaken);
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    /**
     * 获取用户学习进度
     */
    @ApiOperation(value = "获取用户学习进度", notes = "获取用户的学习进度数据")
    @GetMapping("/progress")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserProgress(
            @ApiParam(value = "用户ID", required = true) @PathVariable Integer userId) {
        // 检查用户是否存在
        User user = userService.queryUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }
        
        Map<String, Object> progress = new HashMap<>();
        
        // 单词学习进度
        Map<String, Object> wordProgress = new HashMap<>();
        int learnedWords = userWordMapper.queryUserLearnedWordsCount(userId);
        int totalWords = wordService.getTotalWordsCount();
        int wordPercentage = calculatePercentage(learnedWords, totalWords);
        wordProgress.put("completed", learnedWords);
        wordProgress.put("total", totalWords);
        wordProgress.put("percentage", wordPercentage);
        
        // 听力练习进度
        Map<String, Object> listeningProgress = new HashMap<>();
        int completedListening = listenService.queryUserCompletedListeningCount(userId);
        int totalListening = listenService.queryTotalListeningCount();
        int listeningPercentage = calculatePercentage(completedListening, totalListening);
        listeningProgress.put("completed", completedListening);
        listeningProgress.put("total", totalListening);
        listeningProgress.put("percentage", listeningPercentage);
        
        // 阅读进度（假设有相关服务）
        Map<String, Object> readingProgress = new HashMap<>();
        int completedReading = 3; // 这里需要实际实现
        int totalReading = 15;    // 这里需要实际实现
        int readingPercentage = calculatePercentage(completedReading, totalReading);
        readingProgress.put("completed", completedReading);
        readingProgress.put("total", totalReading);
        readingProgress.put("percentage", readingPercentage);
        
        // 作文进度（假设有相关服务）
        Map<String, Object> essayProgress = new HashMap<>();
        int completedEssays = 2; // 这里需要实际实现
        int totalEssays = 10;    // 这里需要实际实现
        int essayPercentage = calculatePercentage(completedEssays, totalEssays);
        essayProgress.put("completed", completedEssays);
        essayProgress.put("total", totalEssays);
        essayProgress.put("percentage", essayPercentage);
        
        progress.put("words", wordProgress);
        progress.put("listening", listeningProgress);
        progress.put("reading", readingProgress);
        progress.put("essays", essayProgress);
        
        return ResponseEntity.ok(ApiResponse.success(progress));
    }
    
    /**
     * 计算百分比
     */
    private int calculatePercentage(int completed, int total) {
        if (total == 0) return 0;
        int percentage = (int) ((completed * 100.0) / total);
        return Math.min(percentage, 100); // 确保不超过100%
    }
    
    /**
     * 计算连续学习天数（示例实现，实际需要根据数据库记录计算）
     */
    private int calculateDaysStreak(Integer userId) {
        // 这里应该实现一个算法来计算用户的连续学习天数
        // 可以基于用户活动表中的记录来计算
        // 简单起见，这里返回一个固定值
        return 7;
    }
}
