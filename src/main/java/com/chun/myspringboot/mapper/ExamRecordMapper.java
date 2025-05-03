package com.chun.myspringboot.mapper;

import com.chun.myspringboot.pojo.ExamRecord;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ExamRecordMapper {
    // 添加考试记录
    int addRecord(ExamRecord record);

    // 更新考试记录
    int updateRecord(ExamRecord record);

    // 删除考试记录
    int deleteRecord(Integer recordId);

    // 根据ID查询考试记录
    ExamRecord queryRecordById(Integer recordId);

    // 查询用户的所有考试记录
    List<ExamRecord> queryRecordsByUserId(Integer userId);

    // 查询考试的所有记录
    List<ExamRecord> queryRecordsByExamId(Integer examId);

    // 查询用户在某考试的记录
    ExamRecord queryRecordByUserIdAndExamId(Integer userId, Integer examId);

    // 查询所有考试记录
    List<ExamRecord> queryAllRecords();

    // 根据状态查询考试记录
    List<ExamRecord> queryRecordsByStatus(Integer status);
}
