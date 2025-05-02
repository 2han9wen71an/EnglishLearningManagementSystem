package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.mapper.DialogueScenarioMapper;
import com.chun.myspringboot.pojo.DialogueScenario;
import com.chun.myspringboot.service.DialogueScenarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DialogueScenarioServiceImpl implements DialogueScenarioService {
    
    @Autowired
    private DialogueScenarioMapper dialogueScenarioMapper;
    
    @Override
    public DialogueScenario queryScenarioById(Integer scenarioId) {
        return dialogueScenarioMapper.queryScenarioById(scenarioId);
    }
    
    @Override
    public List<DialogueScenario> queryAllScenarios() {
        return dialogueScenarioMapper.queryAllScenarios();
    }
    
    @Override
    public List<DialogueScenario> queryScenariosByDifficulty(Integer difficultyLevel) {
        return dialogueScenarioMapper.queryScenariosByDifficulty(difficultyLevel);
    }
    
    @Override
    public int addScenario(DialogueScenario scenario) {
        return dialogueScenarioMapper.addScenario(scenario);
    }
    
    @Override
    public int updateScenario(DialogueScenario scenario) {
        return dialogueScenarioMapper.updateScenario(scenario);
    }
    
    @Override
    public int deleteScenario(Integer scenarioId) {
        return dialogueScenarioMapper.deleteScenario(scenarioId);
    }
}
