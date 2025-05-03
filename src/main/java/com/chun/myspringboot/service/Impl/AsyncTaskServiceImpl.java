package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.pojo.ExamAnswer;
import com.chun.myspringboot.pojo.ExamQuestion;
import com.chun.myspringboot.service.AIEssayScoreService;
import com.chun.myspringboot.service.AsyncTaskService;
import com.chun.myspringboot.service.ExamAnswerService;
import com.chun.myspringboot.service.ExamQuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 异步任务处理服务实现类
 */
@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncTaskServiceImpl.class);
    
    @Autowired
    private ExamAnswerService examAnswerService;
    
    @Autowired
    private ExamQuestionService examQuestionService;
    
    @Autowired
    private AIEssayScoreService aiEssayScoreService;
    
    @Override
    @Async("taskExecutor")
    public void executeAIScoring(Integer answerId) {
        try {
            logger.info("开始执行AI评分任务，答题ID: {}", answerId);
            
            // 获取答题记录
            ExamAnswer answer = examAnswerService.queryAnswerById(answerId);
            if (answer == null) {
                logger.error("未找到答题记录，答题ID: {}", answerId);
                return;
            }
            
            // 获取题目信息
            ExamQuestion question = examQuestionService.queryQuestionById(answer.getQuestionId());
            if (question == null) {
                logger.error("未找到题目信息，题目ID: {}", answer.getQuestionId());
                return;
            }
            
            // 检查是否是简答题
            if (question.getQuestionType() != 5) {
                logger.info("非简答题，不需要AI评分，题目类型: {}", question.getQuestionType());
                return;
            }
            
            // 使用AI进行评分
            int score = aiEssayScoreService.scoreEssay(
                answer.getUserAnswer(), 
                question.getCorrectAnswer(), 
                question.getScore()
            );
            
            // 获取AI评分解释
            String explanation = aiEssayScoreService.getScoreExplanation(
                answer.getUserAnswer(), 
                question.getCorrectAnswer(), 
                score, 
                question.getScore()
            );
            
            // 更新答题记录
            answer.setScore(score);
            answer.setIsCorrect(score > 0 ? 1 : 0);
            answer.setScoringStatus(3); // AI评分待确认
            answer.setAiScoreExplanation(explanation);
            
            examAnswerService.updateAnswer(answer);
            
            logger.info("AI评分任务完成，答题ID: {}, 得分: {}", answerId, score);
        } catch (Exception e) {
            logger.error("AI评分任务执行失败，答题ID: " + answerId, e);
        }
    }
}
