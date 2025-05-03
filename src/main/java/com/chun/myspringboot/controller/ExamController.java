package com.chun.myspringboot.controller;

import com.chun.myspringboot.pojo.*;
import com.chun.myspringboot.service.Impl.ExamAnswerServiceImpl;
import com.chun.myspringboot.service.Impl.ExamQuestionServiceImpl;
import com.chun.myspringboot.service.Impl.ExamRecordServiceImpl;
import com.chun.myspringboot.service.Impl.ExamServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class ExamController {

    @Autowired
    private ExamServiceImpl examService;

    @Autowired
    private ExamQuestionServiceImpl examQuestionService;

    @Autowired
    private ExamRecordServiceImpl examRecordService;

    @Autowired
    private ExamAnswerServiceImpl examAnswerService;

    /**
     * 查看所有考试
     */
    @RequestMapping("/viewExams")
    public String viewExams(Model model) {
        List<Exam> exams = examService.queryEnabledExams();
        model.addAttribute("exams", exams);
        return "user/exam/exam-list";
    }

    /**
     * 查看考试详情
     */
    @RequestMapping("/viewExam/{examId}")
    public String viewExam(@PathVariable("examId") Integer examId, Model model, HttpSession session) {
        // 获取考试信息
        Exam exam = examService.queryExamById(examId);
        if (exam == null || exam.getStatus() == 0) {
            return "redirect:/viewExams";
        }

        model.addAttribute("exam", exam);

        // 获取用户信息
        User user = (User) session.getAttribute("loginUser");
        if (user != null) {
            // 查询用户是否有考试记录
            ExamRecord record = examRecordService.queryRecordByUserIdAndExamId(user.getUserId(), examId);
            model.addAttribute("record", record);
        }

        return "user/exam/exam-detail";
    }

    /**
     * 开始考试
     */
    @RequestMapping("/startExam/{examId}")
    public String startExam(@PathVariable("examId") Integer examId, HttpSession session) {
        // 获取用户信息
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/";
        }

        // 获取考试信息
        Exam exam = examService.queryExamById(examId);
        if (exam == null || exam.getStatus() == 0) {
            return "redirect:/viewExams";
        }

        // 创建考试记录
        ExamRecord record = examRecordService.startExam(user.getUserId(), examId);

        return "redirect:/doExam/" + record.getRecordId();
    }

    /**
     * 进行考试
     */
    @RequestMapping("/doExam/{recordId}")
    public String doExam(@PathVariable("recordId") Integer recordId, Model model, HttpSession session) {
        // 获取用户信息
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/";
        }

        // 获取考试记录
        ExamRecord record = examRecordService.queryRecordById(recordId);
        if (record == null || !record.getUserId().equals(user.getUserId())) {
            return "redirect:/viewExams";
        }

        // 如果考试已完成，跳转到结果页面
        if (record.getStatus() != 0) {
            return "redirect:/examResult/" + recordId;
        }

        // 获取考试信息
        Exam exam = examService.queryExamById(record.getExamId());
        model.addAttribute("exam", exam);
        model.addAttribute("record", record);

        // 获取考试题目
        List<ExamQuestion> questions = examQuestionService.queryQuestionsByExamId(record.getExamId());

        // 解析题目选项的JSON字符串
        for (ExamQuestion question : questions) {
            if ((question.getQuestionType() == 1 || question.getQuestionType() == 2) && question.getOptions() != null) {
                try {
                    // 使用Jackson解析JSON字符串
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<String, String> optionsMap = objectMapper.readValue(question.getOptions(), Map.class);
                    // 将解析后的Map添加到model中
                    question.setOptions(objectMapper.writeValueAsString(optionsMap));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        model.addAttribute("questions", questions);

        return "user/exam/do-exam";
    }

    /**
     * 提交考试
     */
    @PostMapping("/submitExam")
    @ResponseBody
    public Map<String, Object> submitExam(@RequestParam("recordId") Integer recordId,
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

        // 提交考试
        record = examRecordService.submitExam(recordId, questionIds, answers);

        if (record != null) {
            result.put("success", true);
            result.put("message", "提交成功");
            result.put("recordId", record.getRecordId());
        } else {
            result.put("success", false);
            result.put("message", "提交失败，请稍后重试");
        }

        return result;
    }

    /**
     * 查看考试结果
     */
    @RequestMapping("/examResult/{recordId}")
    public String examResult(@PathVariable("recordId") Integer recordId, Model model, HttpSession session) {
        // 获取用户信息
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/";
        }

        // 获取考试记录
        ExamRecord record = examRecordService.queryRecordById(recordId);
        if (record == null || !record.getUserId().equals(user.getUserId())) {
            return "redirect:/viewExams";
        }

        // 获取考试信息
        Exam exam = examService.queryExamById(record.getExamId());
        model.addAttribute("exam", exam);
        model.addAttribute("record", record);

        // 获取答题记录
        List<ExamAnswer> answers = examAnswerService.queryAnswersByRecordId(recordId);

        // 解析答题记录中的选项JSON字符串
        for (ExamAnswer answer : answers) {
            if ((answer.getQuestionType() == 1 || answer.getQuestionType() == 2) && answer.getOptions() != null) {
                try {
                    // 使用Jackson解析JSON字符串
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<String, String> optionsMap = objectMapper.readValue(answer.getOptions(), Map.class);
                    // 将解析后的Map添加到model中
                    answer.setOptions(objectMapper.writeValueAsString(optionsMap));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        model.addAttribute("answers", answers);

        return "user/exam/exam-result";
    }

    /**
     * 查看历史考试记录
     */
    @RequestMapping("/examHistory")
    public String examHistory(Model model, HttpSession session) {
        // 获取用户信息
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/";
        }

        // 获取用户的考试记录
        List<ExamRecord> records = examRecordService.queryRecordsByUserId(user.getUserId());
        model.addAttribute("records", records);

        return "user/exam/exam-history";
    }
}
