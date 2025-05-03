package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.mapper.ExamMapper;
import com.chun.myspringboot.pojo.Exam;
import com.chun.myspringboot.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamServiceImpl implements ExamService {
    
    @Autowired
    private ExamMapper examMapper;
    
    @Override
    public int addExam(Exam exam) {
        return examMapper.addExam(exam);
    }
    
    @Override
    public int deleteExam(Integer examId) {
        return examMapper.deleteExam(examId);
    }
    
    @Override
    public int updateExam(Exam exam) {
        return examMapper.updateExam(exam);
    }
    
    @Override
    public Exam queryExamById(Integer examId) {
        return examMapper.queryExamById(examId);
    }
    
    @Override
    public List<Exam> queryAllExams() {
        return examMapper.queryAllExams();
    }
    
    @Override
    public List<Exam> queryExamsByGradeId(Integer gradeId) {
        return examMapper.queryExamsByGradeId(gradeId);
    }
    
    @Override
    public List<Exam> queryEnabledExams() {
        return examMapper.queryEnabledExams();
    }
}
