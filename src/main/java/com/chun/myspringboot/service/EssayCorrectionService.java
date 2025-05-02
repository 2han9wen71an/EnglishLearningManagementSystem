package com.chun.myspringboot.service;

import com.chun.myspringboot.pojo.EssayCorrection;

import java.util.List;

public interface EssayCorrectionService {
    // 添加批改结果
    int addCorrection(EssayCorrection correction);
    
    // 删除批改结果
    int deleteCorrection(Integer correctionId);
    
    // 更新批改结果
    int updateCorrection(EssayCorrection correction);
    
    // 根据ID查询批改结果
    EssayCorrection queryCorrectionById(Integer correctionId);
    
    // 根据作文ID查询批改结果
    EssayCorrection queryCorrectionByEssayId(Integer essayId);
    
    // 查询所有批改结果
    List<EssayCorrection> queryAllCorrections();
    
    // 使用AI批改作文
    EssayCorrection correctEssayWithAI(Integer essayId);
}
