package com.chun.myspringboot.service;

import com.chun.myspringboot.pojo.UserActivity;
import java.util.List;

/**
 * 用户活动Service接口
 */
public interface UserActivityService {
    
    /**
     * 添加用户活动
     * @param userActivity 用户活动对象
     * @return 影响的行数
     */
    int addUserActivity(UserActivity userActivity);
    
    /**
     * 删除用户活动
     * @param activityId 活动ID
     * @return 影响的行数
     */
    int deleteUserActivity(Integer activityId);
    
    /**
     * 根据ID查询用户活动
     * @param activityId 活动ID
     * @return 用户活动对象
     */
    UserActivity queryUserActivityById(Integer activityId);
    
    /**
     * 根据用户ID查询用户活动
     * @param userId 用户ID
     * @return 用户活动列表
     */
    List<UserActivity> queryUserActivitiesByUserId(Integer userId);
    
    /**
     * 根据用户ID查询最近的用户活动
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 用户活动列表
     */
    List<UserActivity> queryRecentUserActivitiesByUserId(Integer userId, Integer limit);
    
    /**
     * 根据活动类型查询用户活动
     * @param userId 用户ID
     * @param activityType 活动类型
     * @return 用户活动列表
     */
    List<UserActivity> queryUserActivitiesByType(Integer userId, String activityType);
}
