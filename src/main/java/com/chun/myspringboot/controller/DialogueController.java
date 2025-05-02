package com.chun.myspringboot.controller;

import com.chun.myspringboot.pojo.DialogueContent;
import com.chun.myspringboot.pojo.DialogueHistory;
import com.chun.myspringboot.pojo.DialogueScenario;
import com.chun.myspringboot.service.DeepSeekService;
import com.chun.myspringboot.service.Impl.DialogueContentServiceImpl;
import com.chun.myspringboot.service.Impl.DialogueScenarioServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DialogueController {

    @Autowired
    private DialogueScenarioServiceImpl dialogueScenarioService;

    @Autowired
    private DialogueContentServiceImpl dialogueContentService;

    @Autowired
    private DeepSeekService deepSeekService;

    // 查看所有对话场景
    @RequestMapping("/viewDialogueScenarios")
    public String viewDialogueScenarios(Model model) {
        List<DialogueScenario> scenarios = dialogueScenarioService.queryAllScenarios();
        model.addAttribute("scenarios", scenarios);
        return "user/dialogue/select-scenario";
    }

    // 根据难度级别查看对话场景
    @RequestMapping("/viewDialogueScenariosByDifficulty/{difficultyLevel}")
    public String viewDialogueScenariosByDifficulty(@PathVariable("difficultyLevel") Integer difficultyLevel, Model model) {
        List<DialogueScenario> scenarios = dialogueScenarioService.queryScenariosByDifficulty(difficultyLevel);
        model.addAttribute("scenarios", scenarios);
        model.addAttribute("difficultyLevel", difficultyLevel);
        return "user/dialogue/select-scenario";
    }

    // 进入对话场景
    @RequestMapping("/startDialogue/{scenarioId}")
    public String startDialogue(@PathVariable("scenarioId") Integer scenarioId, Model model, HttpSession session) {
        // 获取场景信息
        DialogueScenario scenario = dialogueScenarioService.queryScenarioById(scenarioId);
        model.addAttribute("scenario", scenario);

        // 获取对话内容（用于提示）
        List<DialogueContent> dialogueContents = dialogueContentService.queryContentByScenarioId(scenarioId);
        model.addAttribute("dialogueContents", dialogueContents);

        // 初始化对话历史
        DialogueHistory dialogueHistory = new DialogueHistory();
        dialogueHistory.setScenarioId(scenarioId);
        dialogueHistory.setScenarioName(scenario.getScenarioName());
        dialogueHistory.setScenarioDescription(scenario.getScenarioDescription());

        // 获取初始AI消息
        DialogueContent initialAiMessage = dialogueContentService.queryContentByScenarioIdAndOrderNum(scenarioId, 1);
        if (initialAiMessage != null) {
            dialogueHistory.addMessage(initialAiMessage.getContent());
        }

        // 将对话历史保存到会话中
        session.setAttribute("dialogueHistory", dialogueHistory);

        // 设置当前对话进度为1（第一条AI消息）
        session.setAttribute("currentDialogueStep", 1);

        // 设置AI模式（true表示使用DeepSeek API，false表示使用预设对话）
        session.setAttribute("useAiMode", true);

        return "user/dialogue/dialogue-simulation";
    }

    // 处理用户输入并获取AI回复
    @PostMapping("/processDialogue")
    @ResponseBody
    public Map<String, String> processDialogue(@RequestParam("scenarioId") Integer scenarioId,
                                          @RequestParam("userInput") String userInput,
                                          HttpSession session) {
        Map<String, String> response = new HashMap<>();

        // 检查是否使用AI模式
        Boolean useAiMode = (Boolean) session.getAttribute("useAiMode");

        if (useAiMode != null && useAiMode) {
            // AI模式 - 使用DeepSeek API

            // 获取对话历史
            DialogueHistory dialogueHistory = (DialogueHistory) session.getAttribute("dialogueHistory");
            if (dialogueHistory == null) {
                // 如果对话历史不存在，创建一个新的
                DialogueScenario scenario = dialogueScenarioService.queryScenarioById(scenarioId);
                dialogueHistory = new DialogueHistory();
                dialogueHistory.setScenarioId(scenarioId);
                dialogueHistory.setScenarioName(scenario.getScenarioName());
                dialogueHistory.setScenarioDescription(scenario.getScenarioDescription());
            }

            // 添加用户输入到对话历史
            dialogueHistory.addMessage(userInput);

            // 获取AI回复
            String aiReply = deepSeekService.generateDialogueResponse(
                dialogueHistory.getScenarioName(),
                dialogueHistory.getScenarioDescription(),
                dialogueHistory.getMessages(),
                userInput
            );

            // 添加AI回复到对话历史
            dialogueHistory.addMessage(aiReply);

            // 更新会话中的对话历史
            session.setAttribute("dialogueHistory", dialogueHistory);

            // 获取下一个提示（如果有）
            String nextPrompt = "";
            Integer currentStep = (Integer) session.getAttribute("currentDialogueStep");
            if (currentStep != null) {
                DialogueContent nextContent = dialogueContentService.queryContentByScenarioIdAndOrderNum(scenarioId, currentStep + 3);
                if (nextContent != null) {
                    nextPrompt = nextContent.getPrompt();
                    session.setAttribute("currentDialogueStep", currentStep + 2);
                }
            }

            // 构建响应
            response.put("content", aiReply);
            response.put("prompt", nextPrompt);

        } else {
            // 预设对话模式

            // 获取当前对话步骤
            Integer currentStep = (Integer) session.getAttribute("currentDialogueStep");

            // 获取当前用户应该回复的对话内容
            DialogueContent userDialogue = dialogueContentService.queryContentByScenarioIdAndOrderNum(scenarioId, currentStep + 1);

            // 更新用户输入
            userDialogue.setContent(userInput);
            dialogueContentService.updateContent(userDialogue);

            // 获取下一条AI回复
            DialogueContent aiResponse = dialogueContentService.queryContentByScenarioIdAndOrderNum(scenarioId, currentStep + 2);

            // 获取下一个提示（如果有）
            String nextPrompt = "";
            DialogueContent nextContent = dialogueContentService.queryContentByScenarioIdAndOrderNum(scenarioId, currentStep + 3);
            if (nextContent != null) {
                nextPrompt = nextContent.getPrompt();
            }

            // 更新当前对话步骤
            session.setAttribute("currentDialogueStep", currentStep + 2);

            // 构建响应
            response.put("content", aiResponse.getContent());
            response.put("prompt", nextPrompt);
        }

        return response;
    }

    // 重置对话
    @PostMapping("/resetDialogue")
    @ResponseBody
    public Map<String, String> resetDialogue(@RequestParam("scenarioId") Integer scenarioId, HttpSession session) {
        Map<String, String> response = new HashMap<>();

        // 重置当前对话步骤为1
        session.setAttribute("currentDialogueStep", 1);

        // 获取场景信息
        DialogueScenario scenario = dialogueScenarioService.queryScenarioById(scenarioId);

        // 获取第一条AI消息
        DialogueContent initialMessage = dialogueContentService.queryContentByScenarioIdAndOrderNum(scenarioId, 1);

        // 重置对话历史
        DialogueHistory dialogueHistory = new DialogueHistory();
        dialogueHistory.setScenarioId(scenarioId);
        dialogueHistory.setScenarioName(scenario.getScenarioName());
        dialogueHistory.setScenarioDescription(scenario.getScenarioDescription());

        // 添加初始AI消息到对话历史
        if (initialMessage != null) {
            dialogueHistory.addMessage(initialMessage.getContent());
        }

        // 更新会话中的对话历史
        session.setAttribute("dialogueHistory", dialogueHistory);

        // 获取下一个提示（如果有）
        String nextPrompt = "";
        DialogueContent nextContent = dialogueContentService.queryContentByScenarioIdAndOrderNum(scenarioId, 2);
        if (nextContent != null) {
            nextPrompt = nextContent.getPrompt();
        }

        // 构建响应
        response.put("content", initialMessage != null ? initialMessage.getContent() : "");
        response.put("prompt", nextPrompt);

        return response;
    }

    // 切换AI模式
    @PostMapping("/toggleAiMode")
    @ResponseBody
    public Map<String, Object> toggleAiMode(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        // 获取当前AI模式
        Boolean useAiMode = (Boolean) session.getAttribute("useAiMode");

        // 切换模式
        useAiMode = (useAiMode == null || !useAiMode);
        session.setAttribute("useAiMode", useAiMode);

        // 返回新的模式
        response.put("useAiMode", useAiMode);
        response.put("message", useAiMode ? "已切换到AI对话模式" : "已切换到预设对话模式");

        return response;
    }
}
