package com.chun.myspringboot.mapper;

import com.chun.myspringboot.pojo.PronunciationAssessment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PronunciationAssessmentMapper {
    // 添加评测记录
    int addAssessment(PronunciationAssessment assessment);
    
    // 删除评测记录
    int deleteAssessment(Integer assessmentId);
    
    // 更新评测记录
    int updateAssessment(PronunciationAssessment assessment);
    
    // 根据ID查询评测记录
    PronunciationAssessment queryAssessmentById(Integer assessmentId);
    
    // 查询用户的所有评测记录
    List<PronunciationAssessment> queryAssessmentsByUserId(Integer userId);
    
    // 查询所有评测记录
    List<PronunciationAssessment> queryAllAssessments();
}
