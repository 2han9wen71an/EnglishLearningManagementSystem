package com.chun.myspringboot.pojo;

import lombok.Data;

/**
 * 考试题目实体类
 */
@Data
public class ExamQuestion {
    private Integer questionId;        // 题目ID
    private Integer examId;            // 所属考试ID
    private Integer questionType;      // 题目类型：1单选题，2多选题，3判断题，4填空题，5简答题
    private String questionContent;    // 题目内容
    private String options;            // 选项(JSON格式)
    private String correctAnswer;      // 正确答案
    private Integer score;             // 分值
    private String analysis;           // 解析
    private Integer orderNum;          // 题目顺序
    
    // 非数据库字段
    private String examTitle;          // 考试标题
}
