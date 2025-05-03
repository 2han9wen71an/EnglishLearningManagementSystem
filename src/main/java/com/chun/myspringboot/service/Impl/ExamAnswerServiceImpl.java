package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.mapper.ExamAnswerMapper;
import com.chun.myspringboot.pojo.ExamAnswer;
import com.chun.myspringboot.service.ExamAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamAnswerServiceImpl implements ExamAnswerService {
    
    @Autowired
    private ExamAnswerMapper examAnswerMapper;
    
    @Override
    public int addAnswer(ExamAnswer answer) {
        return examAnswerMapper.addAnswer(answer);
    }
    
    @Override
    public int batchAddAnswers(List<ExamAnswer> answers) {
        return examAnswerMapper.batchAddAnswers(answers);
    }
    
    @Override
    public int updateAnswer(ExamAnswer answer) {
        return examAnswerMapper.updateAnswer(answer);
    }
    
    @Override
    public int deleteAnswer(Integer answerId) {
        return examAnswerMapper.deleteAnswer(answerId);
    }
    
    @Override
    public int deleteAnswersByRecordId(Integer recordId) {
        return examAnswerMapper.deleteAnswersByRecordId(recordId);
    }
    
    @Override
    public ExamAnswer queryAnswerById(Integer answerId) {
        return examAnswerMapper.queryAnswerById(answerId);
    }
    
    @Override
    public List<ExamAnswer> queryAnswersByRecordId(Integer recordId) {
        return examAnswerMapper.queryAnswersByRecordId(recordId);
    }
    
    @Override
    public ExamAnswer queryAnswerByRecordIdAndQuestionId(Integer recordId, Integer questionId) {
        return examAnswerMapper.queryAnswerByRecordIdAndQuestionId(recordId, questionId);
    }
}
