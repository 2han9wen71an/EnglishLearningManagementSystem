package com.chun.myspringboot.service;

import com.chun.myspringboot.pojo.ExamQuestion;
import java.util.List;

public interface ExamQuestionService {
    // 添加题目
    int addQuestion(ExamQuestion question);
    
    // 批量添加题目
    int batchAddQuestions(List<ExamQuestion> questions);
    
    // 删除题目
    int deleteQuestion(Integer questionId);
    
    // 删除考试的所有题目
    int deleteQuestionsByExamId(Integer examId);
    
    // 更新题目
    int updateQuestion(ExamQuestion question);
    
    // 根据ID查询题目
    ExamQuestion queryQuestionById(Integer questionId);
    
    // 查询考试的所有题目
    List<ExamQuestion> queryQuestionsByExamId(Integer examId);
    
    // 查询考试的题目数量
    int countQuestionsByExamId(Integer examId);
}
