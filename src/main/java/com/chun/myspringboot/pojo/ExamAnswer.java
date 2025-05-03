package com.chun.myspringboot.pojo;

import lombok.Data;

/**
 * 答题记录实体类
 */
@Data
public class ExamAnswer {
    private Integer answerId;      // 答题ID
    private Integer recordId;      // 考试记录ID
    private Integer questionId;    // 题目ID
    private String userAnswer;     // 用户答案
    private Integer score;         // 得分
    private Integer isCorrect;     // 是否正确：0错误，1正确
    private Integer scoringStatus; // 评分状态：0自动评分，1待人工评分，2已人工评分，3AI评分待确认

    private String aiScoreExplanation; // AI评分解释

    // 非数据库字段
    private String questionContent; // 题目内容
    private Integer questionType;   // 题目类型
    private String options;         // 选项
    private String correctAnswer;   // 正确答案
    private String analysis;        // 解析
    private Integer maxScore;       // 题目满分（非数据库字段）
}
