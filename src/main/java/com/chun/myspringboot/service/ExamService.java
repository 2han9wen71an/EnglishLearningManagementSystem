package com.chun.myspringboot.service;

import com.chun.myspringboot.pojo.Exam;
import java.util.List;
import java.util.Map;

public interface ExamService {
    // 添加考试
    int addExam(Exam exam);

    // 删除考试
    int deleteExam(Integer examId);

    // 更新考试
    int updateExam(Exam exam);

    // 根据ID查询考试
    Exam queryExamById(Integer examId);

    // 查询所有考试
    List<Exam> queryAllExams();

    // 根据等级ID查询考试
    List<Exam> queryExamsByGradeId(Integer gradeId);

    // 查询启用状态的考试
    List<Exam> queryEnabledExams();

    /**
     * 获取用户参加的考试次数
     * @param userId 用户ID
     * @return 考试次数
     */
    int queryUserExamCount(Integer userId);

    /**
     * 获取用户考试平均分
     * @param userId 用户ID
     * @return 平均分
     */
    double queryUserExamAverageScore(Integer userId);

    /**
     * 获取考试总数
     * @return 考试总数
     */
    int queryAllExamsCount();

    /**
     * 获取考试完成情况统计
     * @return 考试完成情况统计数据
     */
    Map<String, Integer> getExamCompletionStats();
}
