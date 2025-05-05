package com.chun.myspringboot.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 用户单词关联实体类
 */
@Data
public class UserWord {
    private Integer id;        // 关联ID
    private Integer userId;    // 用户ID
    private Integer wordId;    // 单词ID
    private Integer study;     // 学习状态：0未学习，1已学习
    private Integer remember;  // 记忆状态：0未记住，1已记住
    private Integer collection; // 收藏状态：0未收藏，1已收藏
    private Date learnTime;    // 学习时间
}
