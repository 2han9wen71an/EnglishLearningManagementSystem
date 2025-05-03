package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.mapper.ExamQuestionMapper;
import com.chun.myspringboot.pojo.ExamQuestion;
import com.chun.myspringboot.service.ExamQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamQuestionServiceImpl implements ExamQuestionService {
    
    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    
    @Override
    public int addQuestion(ExamQuestion question) {
        return examQuestionMapper.addQuestion(question);
    }
    
    @Override
    public int batchAddQuestions(List<ExamQuestion> questions) {
        return examQuestionMapper.batchAddQuestions(questions);
    }
    
    @Override
    public int deleteQuestion(Integer questionId) {
        return examQuestionMapper.deleteQuestion(questionId);
    }
    
    @Override
    public int deleteQuestionsByExamId(Integer examId) {
        return examQuestionMapper.deleteQuestionsByExamId(examId);
    }
    
    @Override
    public int updateQuestion(ExamQuestion question) {
        return examQuestionMapper.updateQuestion(question);
    }
    
    @Override
    public ExamQuestion queryQuestionById(Integer questionId) {
        return examQuestionMapper.queryQuestionById(questionId);
    }
    
    @Override
    public List<ExamQuestion> queryQuestionsByExamId(Integer examId) {
        return examQuestionMapper.queryQuestionsByExamId(examId);
    }
    
    @Override
    public int countQuestionsByExamId(Integer examId) {
        return examQuestionMapper.countQuestionsByExamId(examId);
    }
}
