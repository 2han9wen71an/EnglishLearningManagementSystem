package com.chun.myspringboot.service;

import com.chun.myspringboot.pojo.WordCard;

/**
 * 单词卡片服务接口
 */
public interface WordCardService {

    /**
     * 生成单词卡片
     * @param word 单词
     * @return 单词卡片对象
     */
    WordCard generateWordCard(String word);

    /**
     * 保存单词卡片
     * @param wordCard 单词卡片对象
     * @return 保存结果
     */
    int saveWordCard(WordCard wordCard);

    /**
     * 根据单词查询单词卡片
     * @param word 单词
     * @return 单词卡片对象
     */
    WordCard queryWordCardByWord(String word);

    /**
     * 根据用户ID查询单词卡片
     * @param userId 用户ID
     * @return 单词卡片列表
     */
    java.util.List<WordCard> queryWordCardsByUserId(Integer userId);
}
