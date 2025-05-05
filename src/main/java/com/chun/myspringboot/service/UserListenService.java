package com.chun.myspringboot.service;

import com.chun.myspringboot.pojo.UserListen;

import java.util.List;

/**
 * 用户听力练习关联服务接口
 */
public interface UserListenService {
    
    /**
     * 添加用户听力练习关联
     * @param userListen 用户听力练习关联对象
     * @return 影响的行数
     */
    int addUserListen(UserListen userListen);
    
    /**
     * 删除用户听力练习关联
     * @param id 关联ID
     * @return 影响的行数
     */
    int deleteUserListen(Integer id);
    
    /**
     * 更新用户听力练习关联
     * @param userListen 用户听力练习关联对象
     * @return 影响的行数
     */
    int updateUserListen(UserListen userListen);
    
    /**
     * 根据ID查询用户听力练习关联
     * @param id 关联ID
     * @return 用户听力练习关联对象
     */
    UserListen queryUserListenById(Integer id);
    
    /**
     * 根据用户ID和听力ID查询用户听力练习关联
     * @param userId 用户ID
     * @param listenId 听力ID
     * @return 用户听力练习关联对象
     */
    UserListen queryUserListenByUserIdAndListenId(Integer userId, Integer listenId);
    
    /**
     * 根据用户ID查询用户听力练习关联
     * @param userId 用户ID
     * @return 用户听力练习关联列表
     */
    List<UserListen> queryUserListensByUserId(Integer userId);
    
    /**
     * 根据听力ID查询用户听力练习关联
     * @param listenId 听力ID
     * @return 用户听力练习关联列表
     */
    List<UserListen> queryUserListensByListenId(Integer listenId);
    
    /**
     * 获取用户已完成的听力练习数量
     * @param userId 用户ID
     * @return 已完成的听力练习数量
     */
    int queryUserCompletedListeningCount(Integer userId);
}
