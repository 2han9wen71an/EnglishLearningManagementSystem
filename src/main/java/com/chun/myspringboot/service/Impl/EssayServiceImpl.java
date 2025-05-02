package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.mapper.EssayMapper;
import com.chun.myspringboot.pojo.Essay;
import com.chun.myspringboot.service.EssayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EssayServiceImpl implements EssayService {
    
    @Autowired
    private EssayMapper essayMapper;
    
    @Override
    public int addEssay(Essay essay) {
        return essayMapper.addEssay(essay);
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
