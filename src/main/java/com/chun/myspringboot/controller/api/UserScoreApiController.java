package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.pojo.Exam;
import com.chun.myspringboot.pojo.ExamAnswer;
import com.chun.myspringboot.pojo.ExamQuestion;
import com.chun.myspringboot.pojo.ExamRecord;
import com.chun.myspringboot.service.Impl.ExamAnswerServiceImpl;
import com.chun.myspringboot.service.Impl.ExamQuestionServiceImpl;
import com.chun.myspringboot.service.Impl.ExamRecordServiceImpl;
import com.chun.myspringboot.service.Impl.ExamServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户成绩API控制器
 */
@RestController
@RequestMapping("/api/users/{userId}/scores")
public class UserScoreApiController {

    @Autowired
    private ExamRecordServiceImpl examRecordService;

    @Autowired
    private ExamAnswerServiceImpl examAnswerService;

    @Autowired
    private ExamQuestionServiceImpl examQuestionService;

    @Autowired
    private ExamServiceImpl examService;

    /**
     * 获取用户成绩列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserScoreList(
            @PathVariable Integer userId,
            @RequestParam(required = false) Integer examId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        // 获取用户的所有考试记录
        List<ExamRecord> allRecords = examRecordService.queryRecordsByUserId(userId);

        // 根据条件筛选
        List<ExamRecord> filteredRecords = allRecords.stream()
                .filter(record -> {
                    // 按考试ID筛选
                    if (examId != null && !record.getExamId().equals(examId)) {
                        return false;
                    }

                    // 按状态筛选
                    if (status != null) {
                        // 状态转换：0未通过，1通过，2待批改
                        boolean isPassed = record.getScore() >= examService.queryExamById(record.getExamId()).getPassScore();
                        boolean needsScoring = record.getStatus() == 1; // 已完成但未批改

                        int recordStatus;
                        if (needsScoring) {
                            recordStatus = 2; // 待批改
                        } else if (isPassed) {
                            recordStatus = 1; // 通过
                        } else {
                            recordStatus = 0; // 未通过
                        }

                        if (recordStatus != status) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        // 计算分页
        int total = filteredRecords.size();
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, total);

        List<ExamRecord> pagedRecords;
        if (startIndex < total) {
            pagedRecords = filteredRecords.subList(startIndex, endIndex);
        } else {
            pagedRecords = new ArrayList<>();
        }

        // 转换为前端需要的格式
        List<Map<String, Object>> scoreList = pagedRecords.stream()
                .map(record -> {
                    Map<String, Object> scoreMap = new HashMap<>();
                    scoreMap.put("scoreId", record.getRecordId());
                    scoreMap.put("userId", record.getUserId());
                    scoreMap.put("userName", record.getUserName());
                    scoreMap.put("examId", record.getExamId());
                    scoreMap.put("examTitle", record.getExamTitle());
                    scoreMap.put("score", record.getScore());
                    scoreMap.put("totalScore", record.getTotalScore());

                    // 状态转换：0未通过，1通过，2待批改
                    int status1;
                    if (record.getStatus() == 1) { // 已完成但未批改
                        status1 = 2; // 待批改
                    } else if (record.getScore() >= record.getPassScore()) {
                        status1 = 1; // 通过
                    } else {
                        status1 = 0; // 未通过
                    }
                    scoreMap.put("status", status1);

                    // 格式化提交时间
                    scoreMap.put("submitTime", record.getEndTime() != null ?
                            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(record.getEndTime()) : "");

                    return scoreMap;
                })
                .collect(Collectors.toList());

        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("total", total);
        response.put("records", scoreList);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取用户成绩详情
     */
    @GetMapping("/{scoreId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserScoreDetail(
            @PathVariable Integer userId,
            @PathVariable Integer scoreId) {

        // 获取考试记录
        ExamRecord record = examRecordService.queryRecordById(scoreId);
        if (record == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("成绩记录不存在"));
        }

        // 验证用户ID
        if (!record.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("无权访问此成绩记录"));
        }

        // 获取考试信息
        Exam exam = examService.queryExamById(record.getExamId());
        if (exam == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("考试不存在"));
        }

        // 获取答题记录
        List<ExamAnswer> answers = examAnswerService.queryAnswersByRecordId(scoreId);

        // 获取题目信息
        Map<Integer, ExamQuestion> questionMap = new HashMap<>();
        for (ExamAnswer answer : answers) {
            ExamQuestion question = examQuestionService.queryQuestionById(answer.getQuestionId());
            if (question != null) {
                questionMap.put(question.getQuestionId(), question);
            }
        }

        // 构建详细答题记录
        List<Map<String, Object>> detailAnswers = new ArrayList<>();
        int orderNum = 1;
        for (ExamAnswer answer : answers) {
            ExamQuestion question = questionMap.get(answer.getQuestionId());
            if (question != null) {
                Map<String, Object> detailAnswer = new HashMap<>();
                detailAnswer.put("questionId", question.getQuestionId());
                detailAnswer.put("orderNum", orderNum++);
                detailAnswer.put("questionType", question.getQuestionType());
                detailAnswer.put("questionContent", question.getQuestionContent());
                detailAnswer.put("userAnswer", answer.getUserAnswer());
                detailAnswer.put("correctAnswer", question.getCorrectAnswer());
                detailAnswer.put("score", answer.getScore());
                detailAnswer.put("isCorrect", answer.getIsCorrect() == 1);

                detailAnswers.add(detailAnswer);
            }
        }

        // 构建成绩详情
        Map<String, Object> scoreDetail = new HashMap<>();
        scoreDetail.put("scoreId", record.getRecordId());
        scoreDetail.put("userId", record.getUserId());
        scoreDetail.put("userName", record.getUserName());
        scoreDetail.put("examId", record.getExamId());
        scoreDetail.put("examTitle", record.getExamTitle());
        scoreDetail.put("score", record.getScore());
        scoreDetail.put("totalScore", record.getTotalScore());

        // 状态转换：0未通过，1通过，2待批改
        int status;
        if (record.getStatus() == 1) { // 已完成但未批改
            status = 2; // 待批改
        } else if (record.getScore() >= record.getPassScore()) {
            status = 1; // 通过
        } else {
            status = 0; // 未通过
        }
        scoreDetail.put("status", status);

        // 格式化提交时间
        scoreDetail.put("submitTime", record.getEndTime() != null ?
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(record.getEndTime()) : "");

        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("score", scoreDetail);
        response.put("answers", detailAnswers);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
