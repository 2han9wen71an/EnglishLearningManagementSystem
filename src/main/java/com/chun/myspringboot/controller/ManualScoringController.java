package com.chun.myspringboot.controller;

import com.chun.myspringboot.pojo.ExamAnswer;
import com.chun.myspringboot.pojo.ExamQuestion;
import com.chun.myspringboot.pojo.ExamRecord;
import com.chun.myspringboot.service.ExamAnswerService;
import com.chun.myspringboot.service.ExamQuestionService;
import com.chun.myspringboot.service.ExamRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人工评分控制器
 */
@Controller
public class ManualScoringController {

    @Autowired
    private ExamRecordService examRecordService;

    @Autowired
    private ExamAnswerService examAnswerService;

    @Autowired
    private ExamQuestionService examQuestionService;

    /**
     * 跳转到待评分考试列表页面
     */
    @GetMapping("/admin/pendingScoring")
    public String toPendingScoring(Model model) {
        // 获取所有需要评分的考试记录
        List<ExamRecord> records = new ArrayList<>();

        // 获取状态为"已完成"的考试记录
        List<ExamRecord> pendingRecords = examRecordService.queryRecordsByStatus(1);
        if (pendingRecords != null && !pendingRecords.isEmpty()) {
            records.addAll(pendingRecords);
        }

        // 获取所有考试记录，检查是否包含简答题
        List<ExamRecord> allRecords = examRecordService.queryAllRecords();
        if (allRecords != null) {
            for (ExamRecord record : allRecords) {
                // 跳过已经添加的记录
                if (pendingRecords != null && pendingRecords.contains(record)) {
                    continue;
                }

                // 检查该考试记录是否包含简答题
                List<ExamAnswer> answers = examAnswerService.queryAnswersByRecordId(record.getRecordId());
                boolean hasEssayQuestion = false;

                for (ExamAnswer answer : answers) {
                    // 获取题目信息
                    ExamQuestion question = examQuestionService.queryQuestionById(answer.getQuestionId());
                    if (question != null && question.getQuestionType() == 5) {
                        hasEssayQuestion = true;
                        break;
                    }
                }

                // 如果包含简答题，添加到待评分列表
                if (hasEssayQuestion) {
                    records.add(record);
                }
            }
        }

        model.addAttribute("records", records);
        return "admin/exam/pending-scoring";
    }

    /**
     * 跳转到评分页面
     */
    @GetMapping("/admin/scoreExam/{recordId}")
    public String toScoreExam(@PathVariable Integer recordId, Model model) {
        // 获取考试记录
        ExamRecord record = examRecordService.queryRecordById(recordId);
        if (record == null) {
            return "redirect:/admin/pendingScoring";
        }

        // 获取所有答题记录
        List<ExamAnswer> answers = examAnswerService.queryAnswersByRecordId(recordId);

        // 筛选出待人工评分和AI评分待确认的答题记录
        List<ExamAnswer> pendingAnswers = new ArrayList<>();
        for (ExamAnswer answer : answers) {
            if (answer.getScoringStatus() == 1 || answer.getScoringStatus() == 3) {
                // 获取题目信息，设置题目的原始分值
                ExamQuestion question = examQuestionService.queryQuestionById(answer.getQuestionId());
                if (question != null) {
                    answer.setMaxScore(question.getScore()); // 设置题目满分
                }
                pendingAnswers.add(answer);
            }
        }

        model.addAttribute("record", record);
        model.addAttribute("pendingAnswers", pendingAnswers);
        return "admin/exam/score-exam";
    }

    /**
     * 提交评分
     */
    @PostMapping("/admin/submitScoring")
    @ResponseBody
    public Map<String, Object> submitScoring(@RequestParam Integer recordId,
                                            @RequestParam("answerIds[]") List<Integer> answerIds,
                                            @RequestParam("scores[]") List<Integer> scores) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 更新每个答题记录的分数
            for (int i = 0; i < answerIds.size(); i++) {
                Integer answerId = answerIds.get(i);
                Integer score = scores.get(i);

                // 获取答题记录
                ExamAnswer answer = examAnswerService.queryAnswerById(answerId);
                if (answer != null) {
                    // 更新分数和评分状态
                    answer.setScore(score);
                    answer.setIsCorrect(score > 0 ? 1 : 0); // 如果得分大于0，则视为正确
                    answer.setScoringStatus(2); // 已人工评分

                    // 如果是AI评分待确认状态，保留AI评分解释
                    int oldStatus = answer.getScoringStatus();
                    if (oldStatus != 3) {
                        answer.setAiScoreExplanation(null); // 清除AI评分解释
                    }
                    examAnswerService.updateAnswer(answer);
                }
            }

            // 获取所有答题记录，计算总分
            List<ExamAnswer> allAnswers = examAnswerService.queryAnswersByRecordId(recordId);
            int finalScore = 0;
            boolean allScored = true;

            for (ExamAnswer answer : allAnswers) {
                finalScore += answer.getScore();
                if (answer.getScoringStatus() == 1 || answer.getScoringStatus() == 3) {
                    allScored = false;
                }
            }

            // 更新考试记录
            ExamRecord record = examRecordService.queryRecordById(recordId);
            record.setScore(finalScore);

            // 如果所有题目都已评分，则更新状态为"已批改"
            if (allScored) {
                record.setStatus(2); // 已批改
            }

            examRecordService.updateRecord(record);

            result.put("success", true);
            result.put("message", "评分提交成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "评分提交失败：" + e.getMessage());
        }

        return result;
    }
}
