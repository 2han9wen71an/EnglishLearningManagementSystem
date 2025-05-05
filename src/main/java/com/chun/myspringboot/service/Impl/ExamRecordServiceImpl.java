package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.mapper.ExamAnswerMapper;
import com.chun.myspringboot.mapper.ExamMapper;
import com.chun.myspringboot.mapper.ExamQuestionMapper;
import com.chun.myspringboot.mapper.ExamRecordMapper;
import com.chun.myspringboot.pojo.Exam;
import com.chun.myspringboot.pojo.ExamAnswer;
import com.chun.myspringboot.pojo.ExamQuestion;
import com.chun.myspringboot.pojo.ExamRecord;
import com.chun.myspringboot.service.AIEssayScoreService;
import com.chun.myspringboot.service.ActivityRecordService;
import com.chun.myspringboot.service.AsyncTaskService;
import com.chun.myspringboot.service.ExamRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ExamRecordServiceImpl implements ExamRecordService {

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private ExamAnswerMapper examAnswerMapper;

    @Autowired
    private ExamQuestionMapper examQuestionMapper;

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private AIEssayScoreService aiEssayScoreService;

    @Autowired
    private AsyncTaskService asyncTaskService;

    @Override
    public int addRecord(ExamRecord record) {
        return examRecordMapper.addRecord(record);
    }

    @Override
    public int updateRecord(ExamRecord record) {
        return examRecordMapper.updateRecord(record);
    }

    @Override
    public int deleteRecord(Integer recordId) {
        return examRecordMapper.deleteRecord(recordId);
    }

    @Override
    public ExamRecord queryRecordById(Integer recordId) {
        return examRecordMapper.queryRecordById(recordId);
    }

    @Override
    public List<ExamRecord> queryRecordsByUserId(Integer userId) {
        return examRecordMapper.queryRecordsByUserId(userId);
    }

    @Override
    public List<ExamRecord> queryRecordsByExamId(Integer examId) {
        return examRecordMapper.queryRecordsByExamId(examId);
    }

    @Override
    public ExamRecord queryRecordByUserIdAndExamId(Integer userId, Integer examId) {
        return examRecordMapper.queryRecordByUserIdAndExamId(userId, examId);
    }

    @Override
    public List<ExamRecord> queryAllRecords() {
        return examRecordMapper.queryAllRecords();
    }

    @Override
    public List<ExamRecord> queryRecordsByStatus(Integer status) {
        return examRecordMapper.queryRecordsByStatus(status);
    }

    @Autowired
    private ActivityRecordService activityRecordService;

    @Override
    @Transactional
    public ExamRecord startExam(Integer userId, Integer examId) {
        // 检查是否已有进行中的考试记录
        ExamRecord existingRecord = examRecordMapper.queryRecordByUserIdAndExamId(userId, examId);
        if (existingRecord != null && existingRecord.getStatus() == 0) {
            return existingRecord; // 返回进行中的考试记录
        }

        // 创建新的考试记录
        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setExamId(examId);
        record.setStartTime(new Date());
        record.setStatus(0); // 进行中

        int result = examRecordMapper.addRecord(record);

        // 如果添加成功，记录活动
        if (result > 0) {
            try {
                // 获取考试信息
                Exam exam = examMapper.queryExamById(examId);
                if (exam != null) {
                    // 记录考试活动
                    activityRecordService.recordExamActivity(
                            userId,
                            examId,
                            exam.getTitle()
                    );
                }
            } catch (Exception e) {
                // 记录活动失败不影响主要业务
                e.printStackTrace();
            }
        }

        return record;
    }

    @Override
    @Transactional
    public ExamRecord submitExam(Integer recordId, List<Integer> questionIds, List<String> userAnswers) {
        // 获取考试记录
        ExamRecord record = examRecordMapper.queryRecordById(recordId);
        if (record == null || record.getStatus() != 0) {
            return null; // 记录不存在或已完成
        }

        // 获取考试信息
        Exam exam = examMapper.queryExamById(record.getExamId());

        // 获取所有题目
        List<ExamQuestion> questions = examQuestionMapper.queryQuestionsByExamId(record.getExamId());

        // 创建答题记录列表
        List<ExamAnswer> answers = new ArrayList<>();
        int totalScore = 0;

        // 处理每道题的答案
        for (int i = 0; i < questionIds.size(); i++) {
            Integer questionId = questionIds.get(i);
            String userAnswer = userAnswers.get(i);

            // 查找对应的题目
            ExamQuestion question = null;
            for (ExamQuestion q : questions) {
                if (q.getQuestionId().equals(questionId)) {
                    question = q;
                    break;
                }
            }

            if (question != null) {
                // 创建答题记录
                ExamAnswer answer = new ExamAnswer();
                answer.setRecordId(recordId);
                answer.setQuestionId(questionId);
                answer.setUserAnswer(userAnswer);

                // 根据题目类型判断答案是否正确
                boolean isCorrect = false;
                int score = 0;
                int scoringStatus = 0; // 默认自动评分

                // 根据题目类型采用不同的评判方式
                switch (question.getQuestionType()) {
                    case 1: // 单选题
                    case 2: // 多选题
                    case 3: // 判断题
                        // 选择题使用完全匹配
                        isCorrect = question.getCorrectAnswer().equals(userAnswer);
                        score = isCorrect ? question.getScore() : 0;
                        break;
                    case 4: // 填空题
                        // 填空题使用忽略大小写和空格的匹配
                        if (userAnswer != null && question.getCorrectAnswer() != null) {
                            // 处理多个可能的正确答案（用|分隔）
                            String[] possibleAnswers = question.getCorrectAnswer().split("\\|");
                            for (String possibleAnswer : possibleAnswers) {
                                // 忽略大小写和空格进行比较
                                if (userAnswer.trim().equalsIgnoreCase(possibleAnswer.trim())) {
                                    isCorrect = true;
                                    break;
                                }
                            }
                        }
                        score = isCorrect ? question.getScore() : 0;
                        break;
                    case 5: // 简答题
                        // 设置为待AI评分状态
                        isCorrect = false; // 初始设为未评分
                        score = 0; // 初始分数为0
                        scoringStatus = 1; // 设置为待人工评分
                        break;
                    default:
                        break;
                }

                answer.setIsCorrect(isCorrect ? 1 : 0);
                answer.setScore(score);
                answer.setScoringStatus(scoringStatus);
                totalScore += score;

                answers.add(answer);
            }
        }

        // 批量保存答题记录
        if (!answers.isEmpty()) {
            examAnswerMapper.batchAddAnswers(answers);

            // 对简答题启动异步AI评分任务
            for (ExamAnswer answer : answers) {
                ExamQuestion question = null;
                for (ExamQuestion q : questions) {
                    if (q.getQuestionId().equals(answer.getQuestionId())) {
                        question = q;
                        break;
                    }
                }

                if (question != null && question.getQuestionType() == 5) {
                    // 启动异步AI评分任务
                    asyncTaskService.executeAIScoring(answer.getAnswerId());
                }
            }
        }

        // 检查是否有待人工评分的题目或简答题
        boolean needsManualScoring = false;
        for (ExamAnswer answer : answers) {
            // 检查答题记录的评分状态
            if (answer.getScoringStatus() == 1) { // 待人工评分
                needsManualScoring = true;
                break;
            }

            // 检查题目类型（额外检查，确保简答题被标记为需要人工评分）
            for (ExamQuestion q : questions) {
                if (q.getQuestionId().equals(answer.getQuestionId()) && q.getQuestionType() == 5) {
                    needsManualScoring = true;
                    break;
                }
            }

            if (needsManualScoring) {
                break;
            }
        }

        // 更新考试记录
        record.setEndTime(new Date());
        record.setScore(totalScore);
        // 如果有待人工评分的题目，状态设为"已完成"，否则设为"已批改"
        record.setStatus(needsManualScoring ? 1 : 2); // 1:已完成, 2:已批改
        examRecordMapper.updateRecord(record);

        return record;
    }
}
