package com.chun.myspringboot.service;

/**
 * AI简答题评分服务接口
 */
public interface AIEssayScoreService {
    /**
     * 使用AI评分简答题
     * @param userAnswer 用户答案
     * @param correctAnswer 参考答案
     * @param maxScore 最高分值
     * @return 评分结果（0-maxScore）
     */
    int scoreEssay(String userAnswer, String correctAnswer, int maxScore);
    
    /**
     * 获取AI评分解释
     * @param userAnswer 用户答案
     * @param correctAnswer 参考答案
     * @param score AI给出的分数
     * @param maxScore 最高分值
     * @return 评分解释
     */
    String getScoreExplanation(String userAnswer, String correctAnswer, int score, int maxScore);
}
