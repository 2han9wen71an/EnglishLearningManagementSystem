package com.chun.myspringboot.controller.admin;

import com.chun.myspringboot.pojo.Exam;
import com.chun.myspringboot.pojo.ExamQuestion;
import com.chun.myspringboot.pojo.ExamRecord;
import com.chun.myspringboot.pojo.Grade;
import com.chun.myspringboot.service.Impl.ExamQuestionServiceImpl;
import com.chun.myspringboot.service.Impl.ExamRecordServiceImpl;
import com.chun.myspringboot.service.Impl.ExamServiceImpl;
import com.chun.myspringboot.service.Impl.GradeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminExamController {

    @Autowired
    private ExamServiceImpl examService;

    @Autowired
    private ExamQuestionServiceImpl examQuestionService;

    @Autowired
    private ExamRecordServiceImpl examRecordService;

    @Autowired
    private GradeServiceImpl gradeService;

    /**
     * 查看所有考试
     */
    @RequestMapping("/viewExams")
    public String viewExams(Model model) {
        List<Exam> exams = examService.queryAllExams();
        model.addAttribute("exams", exams);
        return "admin/exam/exam-list";
    }

    /**
     * 去添加考试页面
     */
    @RequestMapping("/toAddExam")
    public String toAddExam(Model model) {
        List<Grade> grades = gradeService.queryAllGrade();
        model.addAttribute("grades", grades);
        return "admin/exam/add-exam";
    }

    /**
     * 添加考试
     */
    @PostMapping("/addExam")
    public String addExam(Exam exam) {
        exam.setCreateTime(new Date());
        examService.addExam(exam);
        return "redirect:/admin/viewExams";
    }

    /**
     * 去编辑考试页面
     */
    @RequestMapping("/toEditExam/{examId}")
    public String toEditExam(@PathVariable("examId") Integer examId, Model model) {
        Exam exam = examService.queryExamById(examId);
        model.addAttribute("exam", exam);

        List<Grade> grades = gradeService.queryAllGrade();
        model.addAttribute("grades", grades);

        return "admin/exam/edit-exam";
    }

    /**
     * 编辑考试
     */
    @PostMapping("/editExam")
    public String editExam(Exam exam) {
        examService.updateExam(exam);
        return "redirect:/admin/viewExams";
    }

    /**
     * 删除考试
     */
    @PostMapping("/deleteExam")
    @ResponseBody
    public Map<String, Object> deleteExam(@RequestParam("examId") Integer examId) {
        Map<String, Object> result = new HashMap<>();

        try {
            int count = examService.deleteExam(examId);
            if (count > 0) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "删除失败，请稍后重试");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "系统错误：" + e.getMessage());
        }

        return result;
    }

    /**
     * 查看考试题目
     */
    @RequestMapping("/viewExamQuestions/{examId}")
    public String viewExamQuestions(@PathVariable("examId") Integer examId, Model model) {
        Exam exam = examService.queryExamById(examId);
        model.addAttribute("exam", exam);

        List<ExamQuestion> questions = examQuestionService.queryQuestionsByExamId(examId);
        model.addAttribute("questions", questions);

        return "admin/exam/question-list";
    }

    /**
     * 去添加题目页面
     */
    @RequestMapping("/toAddQuestion/{examId}")
    public String toAddQuestion(@PathVariable("examId") Integer examId, Model model) {
        Exam exam = examService.queryExamById(examId);
        model.addAttribute("exam", exam);
        return "admin/exam/add-question";
    }

    /**
     * 添加题目
     */
    @PostMapping("/addQuestion")
    public String addQuestion(ExamQuestion question) {
        examQuestionService.addQuestion(question);
        return "redirect:/admin/viewExamQuestions/" + question.getExamId();
    }

    /**
     * 去编辑题目页面
     */
    @RequestMapping("/toEditQuestion/{questionId}")
    public String toEditQuestion(@PathVariable("questionId") Integer questionId, Model model) {
        ExamQuestion question = examQuestionService.queryQuestionById(questionId);
        model.addAttribute("question", question);
        return "admin/exam/edit-question";
    }

    /**
     * 编辑题目
     */
    @PostMapping("/editQuestion")
    public String editQuestion(ExamQuestion question) {
        examQuestionService.updateQuestion(question);
        return "redirect:/admin/viewExamQuestions/" + question.getExamId();
    }

    /**
     * 删除题目
     */
    @PostMapping("/deleteQuestion")
    @ResponseBody
    public Map<String, Object> deleteQuestion(@RequestParam("questionId") Integer questionId) {
        Map<String, Object> result = new HashMap<>();

        try {
            ExamQuestion question = examQuestionService.queryQuestionById(questionId);
            int count = examQuestionService.deleteQuestion(questionId);
            if (count > 0) {
                result.put("success", true);
                result.put("message", "删除成功");
                result.put("examId", question.getExamId());
            } else {
                result.put("success", false);
                result.put("message", "删除失败，请稍后重试");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "系统错误：" + e.getMessage());
        }

        return result;
    }

    /**
     * 查看考试记录
     */
    @RequestMapping("/viewExamRecords")
    public String viewExamRecords(Model model) {
        List<ExamRecord> records = examRecordService.queryAllRecords();
        model.addAttribute("records", records);
        return "admin/exam/record-list";
    }

    /**
     * 查看考试记录详情
     */
    @RequestMapping("/viewExamRecord/{recordId}")
    public String viewExamRecord(@PathVariable("recordId") Integer recordId, Model model) {
        ExamRecord record = examRecordService.queryRecordById(recordId);
        model.addAttribute("record", record);

        Exam exam = examService.queryExamById(record.getExamId());
        model.addAttribute("exam", exam);

        return "admin/exam/record-detail";
    }

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    /**
     * 初始化考试数据库
     */
    @RequestMapping("/initExamDatabase")
    public String initExamDatabase(Model model) {
        try {
            // 读取主SQL文件
            org.springframework.core.io.ClassPathResource resource = new org.springframework.core.io.ClassPathResource("sql/exam_tables.sql");
            java.io.Reader reader = new java.io.InputStreamReader(resource.getInputStream(), java.nio.charset.StandardCharsets.UTF_8);
            String sqlScript = org.springframework.util.FileCopyUtils.copyToString(reader);

            // 分割SQL语句
            java.util.List<String> sqlStatements = java.util.Arrays.asList(sqlScript.split(";"));

            // 执行每条SQL语句
            int successCount = 0;
            for (String sql : sqlStatements) {
                if (!sql.trim().isEmpty()) {
                    try {
                        jdbcTemplate.execute(sql);
                        successCount++;
                    } catch (Exception e) {
                        model.addAttribute("error", "执行SQL语句时出错: " + e.getMessage());
                        model.addAttribute("successCount", successCount);
                        model.addAttribute("totalCount", sqlStatements.size());
                        return "admin/database-init-result";
                    }
                }
            }

            model.addAttribute("success", true);
            model.addAttribute("message", "成功执行了 " + successCount + " 条SQL语句，考试数据库初始化完成！");

        } catch (Exception e) {
            model.addAttribute("error", "读取SQL文件时出错: " + e.getMessage());
        }

        return "admin/database-init-result";
    }
}
