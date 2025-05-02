package com.chun.myspringboot.pojo;

import lombok.Data;
import java.util.Date;

@Data
public class PronunciationAssessment {
    private Integer assessmentId;
    private Integer userId;
    private String content;
    private String audioData;
    private String phonemeErrors;
    private Integer overallScore;
    private Date assessmentTime;
    
    // 非数据库字段，用于前端显示
    private String userName;
}
