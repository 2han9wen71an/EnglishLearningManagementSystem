package com.chun.myspringboot.pojo;

import lombok.Data;
import java.util.Date;

@Data
public class Essay {
    private Integer essayId;
    private Integer userId;
    private String title;
    private String content;
    private Date submitTime;
    private Integer status;
    
    // 非数据库字段，用于前端显示
    private String userName;
}
