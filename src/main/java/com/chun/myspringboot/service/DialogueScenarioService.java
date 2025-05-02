package com.chun.myspringboot.service;

import com.chun.myspringboot.pojo.DialogueScenario;

import java.util.List;

public interface DialogueScenarioService {
    // 根据ID查询场景
    DialogueScenario queryScenarioById(Integer scenarioId);
    
    // 查询所有场景
    List<DialogueScenario> queryAllScenarios();
    
    // 根据难度级别查询场景
    List<DialogueScenario> queryScenariosByDifficulty(Integer difficultyLevel);
    
    // 添加场景
    int addScenario(DialogueScenario scenario);
    
    // 更新场景
    int updateScenario(DialogueScenario scenario);
    
    // 删除场景
    int deleteScenario(Integer scenarioId);
}
