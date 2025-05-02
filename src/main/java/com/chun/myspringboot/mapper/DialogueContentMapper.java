package com.chun.myspringboot.mapper;

import com.chun.myspringboot.pojo.DialogueContent;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface DialogueContentMapper {
    // 根据ID查询对话内容
    DialogueContent queryContentById(Integer contentId);
    
    // 根据场景ID查询对话内容
    List<DialogueContent> queryContentByScenarioId(Integer scenarioId);
    
    // 根据场景ID和顺序号查询对话内容
    DialogueContent queryContentByScenarioIdAndOrderNum(Integer scenarioId, Integer orderNum);
    
    // 添加对话内容
    int addContent(DialogueContent content);
    
    // 更新对话内容
    int updateContent(DialogueContent content);
    
    // 删除对话内容
    int deleteContent(Integer contentId);
    
    // 删除场景的所有对话内容
    int deleteContentByScenarioId(Integer scenarioId);
}
