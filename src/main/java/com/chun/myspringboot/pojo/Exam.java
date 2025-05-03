package com.chun.myspringboot.pojo;

import lombok.Data;
import java.util.Date;

/**
 * 考试实体类
 */
@Data
public class Exam {
    private Integer examId;        // 考试ID
    private String title;          // 考试标题
    private String description;    // 考试描述
    private Integer duration;      // 考试时长(分钟)
    private Integer totalScore;    // 总分
    private Integer passScore;     // 及格分数
    private Integer gradeId;       // 关联的等级ID
    private Date createTime;       // 创建时间
    private Integer status;        // 状态：0禁用，1启用
    
    // 非数据库字段
    private String gradeName;      // 等级名称
}
