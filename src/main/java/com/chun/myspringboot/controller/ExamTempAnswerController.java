package com.chun.myspringboot.controller;

import com.chun.myspringboot.pojo.ExamAnswer;
import com.chun.myspringboot.pojo.ExamRecord;
import com.chun.myspringboot.pojo.User;
import com.chun.myspringboot.service.Impl.ExamAnswerServiceImpl;
import com.chun.myspringboot.service.Impl.ExamRecordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ExamTempAnswerController {

    @Autowired
    private ExamAnswerServiceImpl examAnswerService;

    @Autowired
    private ExamRecordServiceImpl examRecordService;

    /**
     * 保存临时答案
     */
    @PostMapping("/saveTempAnswers")
    @ResponseBody
    public Map<String, Object> saveTempAnswers(@RequestParam("recordId") Integer recordId,
                                              @RequestParam("questionIds[]") List<Integer> questionIds,
                                              @RequestParam("answers[]") List<String> answers,
                                              HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 获取用户信息
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }

        // 获取考试记录
        ExamRecord record = examRecordService.queryRecordById(recordId);
        if (record == null || !record.getUserId().equals(user.getUserId()) || record.getStatus() != 0) {
            result.put("success", false);
            result.put("message", "考试记录无效或已完成");
            return result;
        }

        // 删除之前的临时答案
        examAnswerService.deleteAnswersByRecordId(recordId);

        // 创建新的临时答案
        List<ExamAnswer> tempAnswers = new ArrayList<>();
        for (int i = 0; i < questionIds.size(); i++) {
            Integer questionId = questionIds.get(i);
            String userAnswer = answers.get(i);

            // 创建临时答题记录
            ExamAnswer answer = new ExamAnswer();
            answer.setRecordId(recordId);
            answer.setQuestionId(questionId);
            answer.setUserAnswer(userAnswer);
            answer.setIsCorrect(0); // 临时答案，不判断正确性
            answer.setScore(0); // 临时答案，不计分

            tempAnswers.add(answer);
        }

        // 批量保存临时答案
        if (!tempAnswers.isEmpty()) {
            examAnswerService.batchAddAnswers(tempAnswers);
            result.put("success", true);
            result.put("message", "临时答案保存成功");
        } else {
            result.put("success", false);
            result.put("message", "没有答案可保存");
        }

        return result;
    }

    /**
     * 获取临时答案
     */
    @GetMapping("/getTempAnswers")
    @ResponseBody
    public Map<String, Object> getTempAnswers(@RequestParam("recordId") Integer recordId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 获取用户信息
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }

        // 获取考试记录
        ExamRecord record = examRecordService.queryRecordById(recordId);
        if (record == null || !record.getUserId().equals(user.getUserId()) || record.getStatus() != 0) {
            result.put("success", false);
            result.put("message", "考试记录无效或已完成");
            return result;
        }

        // 获取临时答案
        List<ExamAnswer> answers = examAnswerService.queryAnswersByRecordId(recordId);
        
        // 构建答案映射
        Map<Integer, String> answerMap = new HashMap<>();
        for (ExamAnswer answer : answers) {
            answerMap.put(answer.getQuestionId(), answer.getUserAnswer());
        }

        result.put("success", true);
        result.put("answers", answerMap);
        return result;
    }
}
