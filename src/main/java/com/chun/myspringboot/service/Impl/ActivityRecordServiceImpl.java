package com.chun.myspringboot.service.impl;

import com.chun.myspringboot.mapper.UserActivityMapper;
import com.chun.myspringboot.pojo.UserActivity;
import com.chun.myspringboot.service.ActivityRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 活动记录服务实现类
 */
@Service
public class ActivityRecordServiceImpl implements ActivityRecordService {
    
    @Autowired
    private UserActivityMapper userActivityMapper;
    
    @Override
    public boolean recordWordStudyActivity(Integer userId, Integer wordId, String wordName) {
        String content = "学习了单词 「" + wordName + "」";
        return recordActivity(userId, "word_study", content, wordId);
    }
    
    @Override
    public boolean recordListeningActivity(Integer userId, Integer listenId, String listenName) {
        String content = "完成了听力练习 「" + listenName + "」";
        return recordActivity(userId, "listening", content, listenId);
    }
    
    @Override
    public boolean recordExamActivity(Integer userId, Integer examId, String examTitle) {
        String content = "参加了考试 「" + examTitle + "」";
        return recordActivity(userId, "exam", content, examId);
    }
    
    @Override
    public boolean recordEssayActivity(Integer userId, Integer essayId, String essayTitle) {
        String content = "提交了作文 「" + essayTitle + "」";
        return recordActivity(userId, "essay", content, essayId);
    }
    
    @Override
    public boolean recordReadingActivity(Integer userId, Integer readingId, String readingTitle) {
        String content = "完成了阅读 「" + readingTitle + "」";
        return recordActivity(userId, "reading", content, readingId);
    }
    
    @Override
    public boolean recordPronunciationActivity(Integer userId, Integer assessmentId, String content) {
        String activityContent = "进行了发音评测 「" + content + "」";
        return recordActivity(userId, "pronunciation", activityContent, assessmentId);
    }
    
    @Override
    public boolean recordActivity(Integer userId, String activityType, String activityContent, Integer relatedId) {
        try {
            UserActivity activity = new UserActivity();
            activity.setUserId(userId);
            activity.setActivityType(activityType);
            activity.setActivityContent(activityContent);
            activity.setRelatedId(relatedId);
            activity.setActivityTime(new Date());
            
            int result = userActivityMapper.addUserActivity(activity);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
