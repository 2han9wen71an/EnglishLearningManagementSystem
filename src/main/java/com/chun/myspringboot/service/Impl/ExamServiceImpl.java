package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.mapper.ExamMapper;
import com.chun.myspringboot.mapper.ExamRecordMapper;
import com.chun.myspringboot.pojo.Exam;
import com.chun.myspringboot.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

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

    @Override
    public int queryUserExamCount(Integer userId) {
        return examMapper.queryUserExamCount(userId);
    }

    @Override
    public double queryUserExamAverageScore(Integer userId) {
        return examMapper.queryUserExamAverageScore(userId);
    }

    @Override
    public int queryAllExamsCount() {
        // 获取所有考试数量
        List<Exam> exams = examMapper.queryAllExams();
        return exams != null ? exams.size() : 0;
    }

    @Override
    public Map<String, Integer> getExamCompletionStats() {
        // 这里应该从数据库中获取考试完成情况数据
        // 由于没有现成的方法，这里使用模拟数据
        Map<String, Integer> completionData = new LinkedHashMap<>();

        // 获取所有考试
        List<Exam> exams = queryAllExams();
        if (exams != null && !exams.isEmpty()) {
            // 限制最多显示10个考试
            int limit = Math.min(exams.size(), 10);

            for (int i = 0; i < limit; i++) {
                Exam exam = exams.get(i);
                // 这里应该查询数据库获取实际完成人数，暂时使用随机数模拟
                int completionCount = new Random().nextInt(50) + 10; // 模拟数据
                completionData.put(exam.getTitle(), completionCount);
            }
        }

        return completionData;
    }
}
