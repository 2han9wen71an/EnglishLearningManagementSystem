package com.chun.myspringboot.pojo;

import lombok.Data;
import java.util.Date;

/**
 * 用户活动实体类
 */
@Data
public class UserActivity {
    private Integer activityId;     // 活动ID
    private Integer userId;         // 用户ID
    private String activityType;    // 活动类型
    private String activityContent; // 活动内容描述
    private Integer relatedId;      // 相关记录ID
    private Date activityTime;      // 活动时间
    
    // 非数据库字段
    private String userName;        // 用户名
}
