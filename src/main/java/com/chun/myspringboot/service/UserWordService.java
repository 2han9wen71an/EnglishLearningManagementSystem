package com.chun.myspringboot.service;

import com.chun.myspringboot.pojo.UserWord;

import java.util.List;

/**
 * 用户单词关联服务接口
 */
public interface UserWordService {
    
    /**
     * 添加用户单词关联
     * @param userWord 用户单词关联对象
     * @return 影响的行数
     */
    int addUserWord(UserWord userWord);
    
    /**
     * 删除用户单词关联
     * @param id 关联ID
     * @return 影响的行数
     */
    int deleteUserWord(Integer id);
    
    /**
     * 更新用户单词关联
     * @param userWord 用户单词关联对象
     * @return 影响的行数
     */
    int updateUserWord(UserWord userWord);
    
    /**
     * 根据ID查询用户单词关联
     * @param id 关联ID
     * @return 用户单词关联对象
     */
    UserWord queryUserWordById(Integer id);
    
    /**
     * 根据用户ID和单词ID查询用户单词关联
     * @param userId 用户ID
     * @param wordId 单词ID
     * @return 用户单词关联对象
     */
    UserWord queryUserWordByUserIdAndWordId(Integer userId, Integer wordId);
    
    /**
     * 根据用户ID查询用户单词关联
     * @param userId 用户ID
     * @return 用户单词关联列表
     */
    List<UserWord> queryUserWordsByUserId(Integer userId);
    
    /**
     * 根据单词ID查询用户单词关联
     * @param wordId 单词ID
     * @return 用户单词关联列表
     */
    List<UserWord> queryUserWordsByWordId(Integer wordId);
    
    /**
     * 获取用户已学习的单词数量
     * @param userId 用户ID
     * @return 已学习的单词数量
     */
    int queryUserLearnedWordsCount(Integer userId);
}
