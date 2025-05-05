package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.mapper.UserWordMapper;
import com.chun.myspringboot.mapper.WordMapper;
import com.chun.myspringboot.pojo.UserWord;
import com.chun.myspringboot.pojo.Word;
import com.chun.myspringboot.service.ActivityRecordService;
import com.chun.myspringboot.service.UserWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户单词关联服务实现类
 */
@Service
public class UserWordServiceImpl implements UserWordService {

    @Autowired
    private UserWordMapper userWordMapper;

    @Autowired
    private WordMapper wordMapper;

    @Autowired
    private ActivityRecordService activityRecordService;

    @Override
    public int addUserWord(UserWord userWord) {
        int result = userWordMapper.addUserWord(userWord);

        // 如果添加成功且是学习状态，记录活动
        if (result > 0 && userWord.getStudy() != null && userWord.getStudy() == 1) {
            try {
                // 获取单词信息
                Word word = wordMapper.queryWordById(userWord.getWordId());
                if (word != null) {
                    // 记录单词学习活动
                    activityRecordService.recordWordStudyActivity(
                            userWord.getUserId(),
                            userWord.getWordId(),
                            word.getWordName()
                    );
                }
            } catch (Exception e) {
                // 记录活动失败不影响主要业务
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public int deleteUserWord(Integer id) {
        return userWordMapper.deleteUserWord(id);
    }

    @Override
    public int updateUserWord(UserWord userWord) {
        UserWord oldUserWord = userWordMapper.queryUserWordById(userWord.getId());
        int result = userWordMapper.updateUserWord(userWord);

        // 如果更新成功且学习状态从0变为1，记录活动
        if (result > 0 && oldUserWord != null &&
            (oldUserWord.getStudy() == null || oldUserWord.getStudy() == 0) &&
            userWord.getStudy() != null && userWord.getStudy() == 1) {
            try {
                // 获取单词信息
                Word word = wordMapper.queryWordById(userWord.getWordId());
                if (word != null) {
                    // 记录单词学习活动
                    activityRecordService.recordWordStudyActivity(
                            userWord.getUserId(),
                            userWord.getWordId(),
                            word.getWordName()
                    );
                }
            } catch (Exception e) {
                // 记录活动失败不影响主要业务
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public UserWord queryUserWordById(Integer id) {
        return userWordMapper.queryUserWordById(id);
    }

    @Override
    public UserWord queryUserWordByUserIdAndWordId(Integer userId, Integer wordId) {
        return userWordMapper.queryUserWordByUserIdAndWordId(userId, wordId);
    }

    @Override
    public List<UserWord> queryUserWordsByUserId(Integer userId) {
        return userWordMapper.queryUserWordsByUserId(userId);
    }

    @Override
    public List<UserWord> queryUserWordsByWordId(Integer wordId) {
        return userWordMapper.queryUserWordsByWordId(wordId);
    }

    @Override
    public int queryUserLearnedWordsCount(Integer userId) {
        return userWordMapper.queryUserLearnedWordsCount(userId);
    }
}
