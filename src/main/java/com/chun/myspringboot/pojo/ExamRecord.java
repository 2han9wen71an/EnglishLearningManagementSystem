package com.chun.myspringboot.pojo;

import lombok.Data;
import java.util.Date;

/**
 * 考试记录实体类
 */
@Data
public class ExamRecord {
    private Integer recordId;      // 记录ID
    private Integer userId;        // 用户ID
    private Integer examId;        // 考试ID
    private Date startTime;        // 开始时间
    private Date endTime;          // 结束时间
    private Integer score;         // 得分
    private Integer status;        // 状态：0进行中，1已完成，2已批改
    
    // 非数据库字段
    private String userName;       // 用户名
    private String examTitle;      // 考试标题
    private Integer totalScore;    // 考试总分
    private Integer passScore;     // 及格分数
}
