package com.chun.myspringboot.service;

import com.chun.myspringboot.pojo.ExamAnswer;

/**
 * 异步任务处理服务接口
 */
public interface AsyncTaskService {
    /**
     * 异步执行AI评分任务
     * @param answerId 答题记录ID
     */
    void executeAIScoring(Integer answerId);
}
