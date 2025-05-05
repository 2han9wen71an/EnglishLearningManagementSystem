package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.pojo.User;
import com.chun.myspringboot.service.Impl.ExamServiceImpl;
import com.chun.myspringboot.service.Impl.ListenServiceImpl;
import com.chun.myspringboot.service.Impl.UserServiceImpl;
import com.chun.myspringboot.service.Impl.WordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计数据API控制器
 */
@RestController
@RequestMapping("/api/users/{userId}/statistics")
public class StatisticsApiController {
    
    @Autowired
    private UserServiceImpl userService;
    
    @Autowired
    private WordServiceImpl wordService;
    
    @Autowired
    private ExamServiceImpl examService;
    
    @Autowired
    private ListenServiceImpl listenService;
    
    /**
     * 获取用户单词学习统计数据
     */
    @GetMapping("/learning")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserLearningStats(@PathVariable Integer userId) {
        // 检查用户是否存在
        User user = userService.queryUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }
        
        Map<String, Object> stats = new HashMap<>();
        
        // 获取用户学习的单词数量
        int learnedWords = wordService.queryUserLearnedWordsCount(userId);
        
        // 设置目标单词数量（这里可以根据实际情况调整）
        int targetWords = 200;
        
        // 计算完成百分比
        int percentage = (int) (learnedWords * 100.0 / targetWords);
        if (percentage > 100) {
            percentage = 100;
        }
        
        stats.put("learned", learnedWords);
        stats.put("target", targetWords);
        stats.put("percentage", percentage);
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    /**
     * 获取用户考试统计数据
     */
    @GetMapping("/exams")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserExamStats(@PathVariable Integer userId) {
        // 检查用户是否存在
        User user = userService.queryUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }
        
        Map<String, Object> stats = new HashMap<>();
        
        // 获取用户参加的考试次数
        int examCount = examService.queryUserExamCount(userId);
        
        // 获取用户考试平均分
        double averageScore = examService.queryUserExamAverageScore(userId);
        
        // 计算完成百分比（这里假设满分是100分）
        int percentage = (int) averageScore;
        
        // 根据平均分确定状态
        String status = "success";
        if (averageScore < 60) {
            status = "exception";
        } else if (averageScore < 80) {
            status = "warning";
        }
        
        stats.put("count", examCount);
        stats.put("average", averageScore);
        stats.put("percentage", percentage);
        stats.put("status", status);
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    /**
     * 获取用户听力练习统计数据
     */
    @GetMapping("/listening")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserListeningStats(@PathVariable Integer userId) {
        // 检查用户是否存在
        User user = userService.queryUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }
        
        Map<String, Object> stats = new HashMap<>();
        
        // 获取用户完成的听力练习数量
        int completedListening = listenService.queryUserCompletedListeningCount(userId);
        
        // 获取总听力练习数量
        int totalListening = listenService.queryTotalListeningCount();
        
        // 计算完成百分比
        int percentage = (int) (completedListening * 100.0 / totalListening);
        if (percentage > 100) {
            percentage = 100;
        }
        
        stats.put("completed", completedListening);
        stats.put("total", totalListening);
        stats.put("percentage", percentage);
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    /**
     * 获取用户仪表盘综合数据
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserDashboardStats(@PathVariable Integer userId) {
        // 检查用户是否存在
        User user = userService.queryUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }
        
        Map<String, Object> dashboardData = new HashMap<>();
        
        // 获取单词学习统计
        ResponseEntity<ApiResponse<Map<String, Object>>> learningResponse = getUserLearningStats(userId);
        if (learningResponse.getStatusCode() == HttpStatus.OK) {
            dashboardData.put("wordStats", learningResponse.getBody().getData());
        }
        
        // 获取考试统计
        ResponseEntity<ApiResponse<Map<String, Object>>> examResponse = getUserExamStats(userId);
        if (examResponse.getStatusCode() == HttpStatus.OK) {
            dashboardData.put("examStats", examResponse.getBody().getData());
        }
        
        // 获取听力练习统计
        ResponseEntity<ApiResponse<Map<String, Object>>> listeningResponse = getUserListeningStats(userId);
        if (listeningResponse.getStatusCode() == HttpStatus.OK) {
            dashboardData.put("listeningStats", listeningResponse.getBody().getData());
        }
        
        return ResponseEntity.ok(ApiResponse.success(dashboardData));
    }
}
