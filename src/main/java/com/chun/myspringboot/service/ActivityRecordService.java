package com.chun.myspringboot.service;

/**
 * 活动记录服务接口
 * 用于记录用户的各种活动
 */
public interface ActivityRecordService {
    
    /**
     * 记录单词学习活动
     * @param userId 用户ID
     * @param wordId 单词ID
     * @param wordName 单词名称
     * @return 是否记录成功
     */
    boolean recordWordStudyActivity(Integer userId, Integer wordId, String wordName);
    
    /**
     * 记录听力练习活动
     * @param userId 用户ID
     * @param listenId 听力ID
     * @param listenName 听力名称
     * @return 是否记录成功
     */
    boolean recordListeningActivity(Integer userId, Integer listenId, String listenName);
    
    /**
     * 记录考试活动
     * @param userId 用户ID
     * @param examId 考试ID
     * @param examTitle 考试标题
     * @return 是否记录成功
     */
    boolean recordExamActivity(Integer userId, Integer examId, String examTitle);
    
    /**
     * 记录作文提交活动
     * @param userId 用户ID
     * @param essayId 作文ID
     * @param essayTitle 作文标题
     * @return 是否记录成功
     */
    boolean recordEssayActivity(Integer userId, Integer essayId, String essayTitle);
    
    /**
     * 记录阅读活动
     * @param userId 用户ID
     * @param readingId 阅读ID
     * @param readingTitle 阅读标题
     * @return 是否记录成功
     */
    boolean recordReadingActivity(Integer userId, Integer readingId, String readingTitle);
    
    /**
     * 记录发音评测活动
     * @param userId 用户ID
     * @param assessmentId 评测ID
     * @param content 评测内容
     * @return 是否记录成功
     */
    boolean recordPronunciationActivity(Integer userId, Integer assessmentId, String content);
    
    /**
     * 记录通用活动
     * @param userId 用户ID
     * @param activityType 活动类型
     * @param activityContent 活动内容
     * @param relatedId 相关记录ID
     * @return 是否记录成功
     */
    boolean recordActivity(Integer userId, String activityType, String activityContent, Integer relatedId);
}
