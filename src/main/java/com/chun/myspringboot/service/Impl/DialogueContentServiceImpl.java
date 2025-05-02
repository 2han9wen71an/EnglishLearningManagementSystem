package com.chun.myspringboot.service.Impl;

import com.chun.myspringboot.mapper.DialogueContentMapper;
import com.chun.myspringboot.pojo.DialogueContent;
import com.chun.myspringboot.service.DialogueContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DialogueContentServiceImpl implements DialogueContentService {
    
    @Autowired
    private DialogueContentMapper dialogueContentMapper;
    
    @Override
    public DialogueContent queryContentById(Integer contentId) {
        return dialogueContentMapper.queryContentById(contentId);
    }
    
    @Override
    public List<DialogueContent> queryContentByScenarioId(Integer scenarioId) {
        return dialogueContentMapper.queryContentByScenarioId(scenarioId);
    }
    
    @Override
    public DialogueContent queryContentByScenarioIdAndOrderNum(Integer scenarioId, Integer orderNum) {
        return dialogueContentMapper.queryContentByScenarioIdAndOrderNum(scenarioId, orderNum);
    }
    
    @Override
    public int addContent(DialogueContent content) {
        return dialogueContentMapper.addContent(content);
    }
    
    @Override
    public int updateContent(DialogueContent content) {
        return dialogueContentMapper.updateContent(content);
    }
    
    @Override
    public int deleteContent(Integer contentId) {
        return dialogueContentMapper.deleteContent(contentId);
    }
    
    @Override
    public int deleteContentByScenarioId(Integer scenarioId) {
        return dialogueContentMapper.deleteContentByScenarioId(scenarioId);
    }
}
