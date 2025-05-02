package com.chun.myspringboot.pojo;

import lombok.Data;
import java.util.Date;

@Data
public class EssayCorrection {
    private Integer correctionId;
    private Integer essayId;
    private String correctedContent;
    private String grammarErrors;
    private String vocabularySuggestions;
    private String structureComments;
    private Integer overallScore;
    private Date correctionTime;
    
    // 非数据库字段，用于前端显示
    private Essay essay;
}
