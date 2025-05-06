package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.mapper.UserActivityMapper;
import com.chun.myspringboot.pojo.UserActivity;
import com.chun.myspringboot.service.UserActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户活动Service实现类
 */
@Service
public class UserActivityServiceImpl implements UserActivityService {
    
    @Autowired
    private UserActivityMapper userActivityMapper;
    
    @Override
    public int addUserActivity(UserActivity userActivity) {
        return userActivityMapper.addUserActivity(userActivity);
    }
    
    @Override
    public int deleteUserActivity(Integer activityId) {
        return userActivityMapper.deleteUserActivity(activityId);
    }
    
    @Override
    public UserActivity queryUserActivityById(Integer activityId) {
        return userActivityMapper.queryUserActivityById(activityId);
    }
    
    @Override
    public List<UserActivity> queryUserActivitiesByUserId(Integer userId) {
        return userActivityMapper.queryUserActivitiesByUserId(userId);
    }
    
    @Override
    public List<UserActivity> queryRecentUserActivitiesByUserId(Integer userId, Integer limit) {
        return userActivityMapper.queryRecentUserActivitiesByUserId(userId, limit);
    }
    
    @Override
    public List<UserActivity> queryUserActivitiesByType(Integer userId, String activityType) {
        return userActivityMapper.queryUserActivitiesByType(userId, activityType);
    }
}
