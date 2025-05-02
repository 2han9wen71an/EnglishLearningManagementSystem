package com.chun.myspringboot.mapper;

import com.chun.myspringboot.pojo.WordCard;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface WordCardMapper {
    
    /**
     * 添加单词卡片
     * @param wordCard 单词卡片对象
     * @return 影响的行数
     */
    int addWordCard(WordCard wordCard);
    
    /**
     * 删除单词卡片
     * @param cardId 卡片ID
     * @return 影响的行数
     */
    int deleteWordCard(Integer cardId);
    
    /**
     * 更新单词卡片
     * @param wordCard 单词卡片对象
     * @return 影响的行数
     */
    int updateWordCard(WordCard wordCard);
    
    /**
     * 根据ID查询单词卡片
     * @param cardId 卡片ID
     * @return 单词卡片对象
     */
    WordCard queryWordCardById(Integer cardId);
    
    /**
     * 根据单词查询单词卡片
     * @param word 单词
     * @return 单词卡片对象
     */
    WordCard queryWordCardByWord(String word);
    
    /**
     * 查询所有单词卡片
     * @return 单词卡片列表
     */
    List<WordCard> queryAllWordCards();
    
    /**
     * 根据用户ID查询单词卡片
     * @param userId 用户ID
     * @return 单词卡片列表
     */
    List<WordCard> queryWordCardsByUserId(Integer userId);
}
