package com.chun.myspringboot.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 用户听力练习关联实体类
 */
@Data
public class UserListen {
    private Integer id;            // 关联ID
    private Integer userId;        // 用户ID
    private Integer listenId;      // 听力ID
    private Integer completed;     // 完成状态：0未完成，1已完成
    private Date completionTime;   // 完成时间
}
