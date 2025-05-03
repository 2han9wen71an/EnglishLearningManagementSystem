package com.chun.myspringboot.mapper;

import com.chun.myspringboot.pojo.ExamAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ExamAnswerMapper {
    // 添加答题记录
    int addAnswer(ExamAnswer answer);
    
    // 批量添加答题记录
    int batchAddAnswers(List<ExamAnswer> answers);
    
    // 更新答题记录
    int updateAnswer(ExamAnswer answer);
    
    // 删除答题记录
    int deleteAnswer(Integer answerId);
    
    // 删除考试记录的所有答题记录
    int deleteAnswersByRecordId(Integer recordId);
    
    // 根据ID查询答题记录
    ExamAnswer queryAnswerById(Integer answerId);
    
    // 查询考试记录的所有答题记录
    List<ExamAnswer> queryAnswersByRecordId(Integer recordId);
    
    // 查询考试记录中某题的答题记录
    ExamAnswer queryAnswerByRecordIdAndQuestionId(Integer recordId, Integer questionId);
}
