package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.mapper.UserWordMapper;
import com.chun.myspringboot.mapper.WordMapper;
import com.chun.myspringboot.pojo.User;
import com.chun.myspringboot.service.UserService;
import com.chun.myspringboot.service.WordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 单词统计API控制器
 */
@Api(tags = "单词统计API")
@RestController
@RequestMapping("/api/words/stats")
public class WordStatsApiController {
    
    @Autowired
    private WordService wordService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private WordMapper wordMapper;
    
    @Autowired
    private UserWordMapper userWordMapper;
    
    /**
     * 获取单词统计数据（支持筛选）
     */
    @ApiOperation(value = "获取单词统计数据", notes = "获取单词统计数据，支持筛选")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWordStats(
            @ApiParam(value = "用户ID") @RequestParam(required = false) Integer userId,
            @ApiParam(value = "等级ID") @RequestParam(required = false) Integer gradeId,
            @ApiParam(value = "学习状态") @RequestParam(required = false) Integer status,
            @ApiParam(value = "搜索关键词") @RequestParam(required = false) String query) {
        
        Map<String, Object> stats = new HashMap<>();
        
        // 获取总单词数
        int totalWords = wordService.countSearchWords(query, gradeId, null, null);
        stats.put("totalWords", totalWords);
        
        // 如果提供了用户ID，获取用户相关的统计数据
        if (userId != null) {
            // 检查用户是否存在
            User user = userService.queryUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("用户不存在"));
            }
            
            // 获取已学单词数（根据筛选条件）
            int learnedWords = wordService.countSearchWords(query, gradeId, 1, userId);
            stats.put("learnedWords", learnedWords);
            
            // 获取已掌握单词数（根据筛选条件）
            int masteredWords = wordService.countSearchWords(query, gradeId, 2, userId);
            stats.put("masteredWords", masteredWords);
            
            // 获取今日学习单词数
            Date today = new Date();
            int todayWords = userWordMapper.queryUserTodayLearnedWordsCount(userId, today);
            stats.put("todayWords", todayWords);
            
            // 计算学习进度百分比
            int progressPercentage = 0;
            if (totalWords > 0) {
                progressPercentage = Math.min(100, (int) ((learnedWords * 100.0) / totalWords));
            }
            stats.put("progressPercentage", progressPercentage);
        }
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
