package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.service.ExamService;
import com.chun.myspringboot.service.ListenService;
import com.chun.myspringboot.service.NoticeService;
import com.chun.myspringboot.service.UserService;
import com.chun.myspringboot.service.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 管理员统计数据API控制器
 */
@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private WordService wordService;

    @Autowired
    private ExamService examService;

    @Autowired
    private ListenService listenService;

    @Autowired
    private NoticeService noticeService;

    /**
     * 获取系统统计数据
     */
    @GetMapping("/system")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 获取用户总数
        int userCount = userService.queryAllUserCount();
        stats.put("userCount", userCount);

        // 获取单词总数
        int wordCount = wordService.getTotalWordsCount();
        stats.put("wordCount", wordCount);

        // 获取考试总数
        int examCount = examService.queryAllExamsCount();
        stats.put("examCount", examCount);

        // 获取公告总数
        int noticeCount = noticeService.queryAllNoticeCount();
        stats.put("noticeCount", noticeCount);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 获取用户增长趋势
     */
    @GetMapping("/users/growth")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserGrowthTrend(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        // 如果没有指定开始日期，默认为30天前
        if (startDate == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            startDate = calendar.getTime();
        }

        // 如果没有指定结束日期，默认为今天
        if (endDate == null) {
            endDate = new Date();
        }

        // 获取用户增长趋势数据
        Map<String, Integer> growthData = userService.getUserGrowthTrend(startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        result.put("dates", new ArrayList<>(growthData.keySet()));
        result.put("counts", new ArrayList<>(growthData.values()));

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取考试完成情况
     */
    @GetMapping("/exams/completion")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExamCompletionStats() {
        Map<String, Object> stats = new HashMap<>();

        // 获取考试完成情况数据
        Map<String, Integer> completionData = examService.getExamCompletionStats();

        stats.put("labels", new ArrayList<>(completionData.keySet()));
        stats.put("values", new ArrayList<>(completionData.values()));

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 获取系统活跃度
     */
    @GetMapping("/system/activity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemActivityStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        // 如果没有指定开始日期，默认为7天前
        if (startDate == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -7);
            startDate = calendar.getTime();
        }

        // 如果没有指定结束日期，默认为今天
        if (endDate == null) {
            endDate = new Date();
        }

        // 获取系统活跃度数据
        Map<String, Map<String, Integer>> activityData = userService.getSystemActivityStats(startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        result.put("dates", new ArrayList<>(activityData.keySet()));
        result.put("activities", activityData);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
