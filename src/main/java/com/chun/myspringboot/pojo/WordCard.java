package com.chun.myspringboot.pojo;

import lombok.Data;

/**
 * 单词卡片实体类
 */
@Data
public class WordCard {
    private Integer cardId;        // 卡片ID
    private String word;           // 单词
    private String explanation;    // 解释
    private String imageUrl;       // 图片URL
    private String contextSentence; // 场景例句
    private String memoryTip;      // 记忆口诀
    private Integer userId;        // 用户ID（如果需要关联到特定用户）
    private java.util.Date createTime; // 创建时间
}
