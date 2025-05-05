package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.mapper.EssayMapper;
import com.chun.myspringboot.pojo.Essay;
import com.chun.myspringboot.service.ActivityRecordService;
import com.chun.myspringboot.service.EssayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EssayServiceImpl implements EssayService {

    @Autowired
    private EssayMapper essayMapper;

    @Autowired
    private ActivityRecordService activityRecordService;

    @Override
    public int addEssay(Essay essay) {
        int result = essayMapper.addEssay(essay);

        // 如果添加成功，记录活动
        if (result > 0) {
            try {
                // 记录作文提交活动
                activityRecordService.recordEssayActivity(
                        essay.getUserId(),
                        essay.getEssayId(),
                        essay.getTitle()
                );
            } catch (Exception e) {
                // 记录活动失败不影响主要业务
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public int deleteEssay(Integer essayId) {
        return essayMapper.deleteEssay(essayId);
    }

    @Override
    public int updateEssay(Essay essay) {
        return essayMapper.updateEssay(essay);
    }

    @Override
    public int updateEssayStatus(Integer essayId, Integer status) {
        return essayMapper.updateEssayStatus(essayId, status);
    }

    @Override
    public Essay queryEssayById(Integer essayId) {
        return essayMapper.queryEssayById(essayId);
    }

    @Override
    public List<Essay> queryEssaysByUserId(Integer userId) {
        return essayMapper.queryEssaysByUserId(userId);
    }

    @Override
    public List<Essay> queryAllEssays() {
        return essayMapper.queryAllEssays();
    }
}
