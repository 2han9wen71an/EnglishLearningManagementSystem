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

/**
 * 考试API控制器
 */
@RestController
@RequestMapping("/api/exams")
public class ExamApiController {

    @Autowired
    private ExamServiceImpl examService;

    @Autowired
    private ExamQuestionServiceImpl examQuestionService;

    @Autowired
    private ExamRecordServiceImpl examRecordService;

    @Autowired
    private ExamAnswerServiceImpl examAnswerService;

    /**
     * 获取所有考试
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Exam>>> getAllExams(@RequestParam(required = false, defaultValue = "true") Boolean enabled) {
        List<Exam> exams;
        if (enabled) {
            exams = examService.queryEnabledExams();
        } else {
            exams = examService.queryAllExams();
        }
        return ResponseEntity.ok(ApiResponse.success(exams));
    }

    /**
     * 获取考试详情
     */
    @GetMapping("/{examId}")
    public ResponseEntity<ApiResponse<Exam>> getExamById(@PathVariable Integer examId) {
        Exam exam = examService.queryExamById(examId);
        if (exam != null) {
            return ResponseEntity.ok(ApiResponse.success(exam));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("考试不存在"));
        }
    }

    /**
     * 获取考试题目
     */
    @GetMapping("/{examId}/questions")
    public ResponseEntity<ApiResponse<List<ExamQuestion>>> getExamQuestions(@PathVariable Integer examId) {
        // 检查考试是否存在
        Exam exam = examService.queryExamById(examId);
        if (exam == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("考试不存在"));
        }

        List<ExamQuestion> questions = examQuestionService.queryQuestionsByExamId(examId);

        // 处理选项JSON字符串
        for (ExamQuestion question : questions) {
            if ((question.getQuestionType() == 1 || question.getQuestionType() == 2) && question.getOptions() != null) {
                try {
                    // 使用Jackson解析JSON字符串
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    @SuppressWarnings("unchecked")
                    Map<String, String> optionsMap = objectMapper.readValue(question.getOptions(), Map.class);
                    // 将解析后的Map添加回去
                    question.setOptions(objectMapper.writeValueAsString(optionsMap));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    /**
     * 开始考试
     */
    @PostMapping("/{examId}/start")
    public ResponseEntity<ApiResponse<ExamRecord>> startExam(@PathVariable Integer examId, @RequestParam Integer userId) {
        // 检查考试是否存在
        Exam exam = examService.queryExamById(examId);
        if (exam == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("考试不存在"));
        }

        // 检查是否有进行中的考试记录
        ExamRecord existingRecord = examRecordService.queryRecordByUserIdAndExamId(userId, examId);
        if (existingRecord != null && existingRecord.getStatus() == 0) {
            return ResponseEntity.ok(ApiResponse.success("已有进行中的考试", existingRecord));
        }

        // 创建新的考试记录
        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setExamId(examId);
        record.setStartTime(new Date());
        record.setStatus(0); // 进行中

        int result = examRecordService.addRecord(record);
        if (result > 0) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("考试开始", record));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("开始考试失败，请稍后重试"));
        }
    }

    /**
     * 保存临时答案
     */
    @PostMapping("/records/{recordId}/answers/temp")
    public ResponseEntity<ApiResponse<ExamAnswer>> saveTempAnswer(
            @PathVariable Integer recordId,
            @RequestParam Integer questionId,
            @RequestParam String userAnswer) {

        // 检查考试记录是否存在
        ExamRecord record = examRecordService.queryRecordById(recordId);
        if (record == null || record.getStatus() != 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("考试记录无效或已完成"));
        }

        // 检查题目是否存在
        ExamQuestion question = examQuestionService.queryQuestionById(questionId);
        if (question == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("题目不存在"));
        }

        // 查询是否已存在答案
        ExamAnswer existingAnswer = examAnswerService.queryAnswerByRecordIdAndQuestionId(recordId, questionId);

        if (existingAnswer != null) {
            // 更新已有答案
            existingAnswer.setUserAnswer(userAnswer);
            int result = examAnswerService.updateAnswer(existingAnswer);
            if (result > 0) {
                return ResponseEntity.ok(ApiResponse.success("临时答案更新成功", existingAnswer));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("更新失败，请稍后重试"));
            }
        } else {
            // 创建新答案
            ExamAnswer answer = new ExamAnswer();
            answer.setRecordId(recordId);
            answer.setQuestionId(questionId);
            answer.setUserAnswer(userAnswer);
            answer.setIsCorrect(0); // 未评分
            answer.setScore(0); // 未评分

            int result = examAnswerService.addAnswer(answer);
            if (result > 0) {
                // 重新查询以获取生成的ID
                ExamAnswer savedAnswer = examAnswerService.queryAnswerByRecordIdAndQuestionId(recordId, questionId);
                return ResponseEntity.ok(ApiResponse.success("临时答案保存成功", savedAnswer));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("保存失败，请稍后重试"));
            }
        }
    }

    /**
     * 提交考试
     */
    @PostMapping("/records/{recordId}/submit")
    public ResponseEntity<ApiResponse<ExamRecord>> submitExam(
            @PathVariable Integer recordId,
            @RequestBody Map<String, Object> submitData) {

        // 从请求体中获取题目ID和答案
        @SuppressWarnings("unchecked")
        List<Integer> questionIds = (List<Integer>) submitData.get("questionIds");
        @SuppressWarnings("unchecked")
        List<String> answers = (List<String>) submitData.get("answers");

        // 检查参数
        if (questionIds == null || answers == null || questionIds.size() != answers.size()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("参数错误"));
        }

        // 检查考试记录是否存在
        ExamRecord record = examRecordService.queryRecordById(recordId);
        if (record == null || record.getStatus() != 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("考试记录无效或已完成"));
        }

        // 提交考试
        record = examRecordService.submitExam(recordId, questionIds, answers);
        if (record != null) {
            return ResponseEntity.ok(ApiResponse.success("考试提交成功", record));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("提交失败，请稍后重试"));
        }
    }

    /**
     * 获取考试结果
     */
    @GetMapping("/records/{recordId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExamResult(@PathVariable Integer recordId) {
        // 检查考试记录是否存在
        ExamRecord record = examRecordService.queryRecordById(recordId);
        if (record == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("考试记录不存在"));
        }

        // 获取考试信息
        Exam exam = examService.queryExamById(record.getExamId());
        if (exam == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("考试不存在"));
        }

        // 获取答题记录
        List<ExamAnswer> answers = examAnswerService.queryAnswersByRecordId(recordId);

        // 处理选项JSON字符串
        for (ExamAnswer answer : answers) {
            if ((answer.getQuestionType() == 1 || answer.getQuestionType() == 2) && answer.getOptions() != null) {
                try {
                    // 使用Jackson解析JSON字符串
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    @SuppressWarnings("unchecked")
                    Map<String, String> optionsMap = objectMapper.readValue(answer.getOptions(), Map.class);
                    // 将解析后的Map添加回去
                    answer.setOptions(objectMapper.writeValueAsString(optionsMap));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // 构建结果
        Map<String, Object> result = new HashMap<>();
        result.put("exam", exam);
        result.put("record", record);
        result.put("answers", answers);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取用户的考试记录
     */
    @GetMapping("/records/user/{userId}")
    public ResponseEntity<ApiResponse<List<ExamRecord>>> getUserExamRecords(@PathVariable Integer userId) {
        List<ExamRecord> records = examRecordService.queryRecordsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    /**
     * 管理员：添加考试
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Exam>> addExam(@RequestBody Exam exam) {
        // 设置创建时间
        if (exam.getCreateTime() == null) {
            exam.setCreateTime(new Date());
        }

        int result = examService.addExam(exam);
        if (result > 0) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("考试添加成功", exam));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("添加失败，请稍后重试"));
        }
    }

    /**
     * 管理员：更新考试
     */
    @PutMapping("/{examId}")
    public ResponseEntity<ApiResponse<String>> updateExam(@PathVariable Integer examId, @RequestBody Exam exam) {
        // 确保examId匹配
        if (!examId.equals(exam.getExamId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("考试ID不匹配"));
        }

        // 检查考试是否存在
        Exam existingExam = examService.queryExamById(examId);
        if (existingExam == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("考试不存在"));
        }

        int result = examService.updateExam(exam);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("考试更新成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新失败，请稍后重试"));
        }
    }

    /**
     * 管理员：删除考试
     */
    @DeleteMapping("/{examId}")
    public ResponseEntity<ApiResponse<String>> deleteExam(@PathVariable Integer examId) {
        // 检查考试是否存在
        Exam existingExam = examService.queryExamById(examId);
        if (existingExam == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("考试不存在"));
        }

        int result = examService.deleteExam(examId);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("考试删除成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除失败，请稍后重试"));
        }
    }

    /**
     * 管理员：添加考试题目
     */
    @PostMapping("/{examId}/questions")
    public ResponseEntity<ApiResponse<ExamQuestion>> addExamQuestion(@PathVariable Integer examId, @RequestBody ExamQuestion question) {
        // 检查考试是否存在
        Exam existingExam = examService.queryExamById(examId);
        if (existingExam == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("考试不存在"));
        }

        // 设置考试ID
        question.setExamId(examId);

        int result = examQuestionService.addQuestion(question);
        if (result > 0) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("题目添加成功", question));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("添加失败，请稍后重试"));
        }
    }

    /**
     * 管理员：更新考试题目
     */
    @PutMapping("/questions/{questionId}")
    public ResponseEntity<ApiResponse<String>> updateExamQuestion(@PathVariable Integer questionId, @RequestBody ExamQuestion question) {
        // 确保questionId匹配
        if (!questionId.equals(question.getQuestionId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("题目ID不匹配"));
        }

        // 检查题目是否存在
        ExamQuestion existingQuestion = examQuestionService.queryQuestionById(questionId);
        if (existingQuestion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("题目不存在"));
        }

        int result = examQuestionService.updateQuestion(question);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("题目更新成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新失败，请稍后重试"));
        }
    }

    /**
     * 管理员：删除考试题目
     */
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<ApiResponse<String>> deleteExamQuestion(@PathVariable Integer questionId) {
        // 检查题目是否存在
        ExamQuestion existingQuestion = examQuestionService.queryQuestionById(questionId);
        if (existingQuestion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("题目不存在"));
        }

        int result = examQuestionService.deleteQuestion(questionId);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("题目删除成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除失败，请稍后重试"));
        }
    }
}
