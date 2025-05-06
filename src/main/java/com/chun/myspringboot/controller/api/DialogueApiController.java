package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.pojo.DialogueContent;
import com.chun.myspringboot.pojo.DialogueHistory;
import com.chun.myspringboot.pojo.DialogueScenario;
import com.chun.myspringboot.service.DeepSeekService;
import com.chun.myspringboot.service.Impl.DialogueContentServiceImpl;
import com.chun.myspringboot.service.Impl.DialogueScenarioServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 情景对话API控制器
 */
@RestController
@RequestMapping("/api/dialogues")
public class DialogueApiController {

    @Autowired
    private DialogueScenarioServiceImpl dialogueScenarioService;

    @Autowired
    private DialogueContentServiceImpl dialogueContentService;

    @Autowired
    private DeepSeekService deepSeekService;

    /**
     * 获取所有对话场景
     */
    @GetMapping("/scenarios")
    public ResponseEntity<ApiResponse<List<DialogueScenario>>> getAllScenarios() {
        List<DialogueScenario> scenarios = dialogueScenarioService.queryAllScenarios();
        return ResponseEntity.ok(ApiResponse.success(scenarios));
    }

    /**
     * 根据难度级别获取对话场景
     */
    @GetMapping("/scenarios/difficulty/{level}")
    public ResponseEntity<ApiResponse<List<DialogueScenario>>> getScenariosByDifficulty(@PathVariable Integer level) {
        List<DialogueScenario> scenarios = dialogueScenarioService.queryScenariosByDifficulty(level);
        return ResponseEntity.ok(ApiResponse.success(scenarios));
    }

    /**
     * 获取对话场景详情
     */
    @GetMapping("/scenarios/{scenarioId}")
    public ResponseEntity<ApiResponse<DialogueScenario>> getScenarioById(@PathVariable Integer scenarioId) {
        DialogueScenario scenario = dialogueScenarioService.queryScenarioById(scenarioId);
        if (scenario != null) {
            return ResponseEntity.ok(ApiResponse.success(scenario));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("对话场景不存在"));
        }
    }

    /**
     * 处理用户对话输入
     */
    @PostMapping("/process")
    public ResponseEntity<ApiResponse<Map<String, String>>> processDialogue(
            @RequestParam Integer scenarioId,
            @RequestParam String userInput,
            @RequestParam(required = false, defaultValue = "true") Boolean useAiMode,
            @RequestParam(required = false) String initialMessage,
            @RequestParam(required = false) String initialPrompt,
            @RequestParam(required = false) String sessionId) {

        Map<String, String> response = new HashMap<>();

        try {
            // 获取场景信息
            DialogueScenario scenario = dialogueScenarioService.queryScenarioById(scenarioId);
            if (scenario == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("对话场景不存在"));
            }

            if (useAiMode) {
                // AI模式 - 使用DeepSeek API

                // 创建对话历史对象
                DialogueHistory dialogueHistory = new DialogueHistory();
                dialogueHistory.setScenarioId(scenarioId);
                dialogueHistory.setScenarioName(scenario.getScenarioName());
                dialogueHistory.setScenarioDescription(scenario.getScenarioDescription());

                // 如果有初始消息，先添加到对话历史
                if (initialMessage != null && !initialMessage.isEmpty()) {
                    dialogueHistory.addMessage(initialMessage);
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

                // 获取下一个提示（如果有）
                String nextPrompt = "";
                // 尝试从数据库获取下一个提示
                Integer currentStep = 1; // 默认从第一步开始

                // 如果传入了sessionId，尝试从会话中获取当前步骤
                if (sessionId != null && !sessionId.isEmpty()) {
                    // 这里可以实现从Redis或其他会话存储中获取currentStep
                    // 暂时使用默认值
                }

                DialogueContent nextContent = dialogueContentService.queryContentByScenarioIdAndOrderNum(scenarioId, currentStep + 3);
                if (nextContent != null) {
                    nextPrompt = nextContent.getPrompt();
                    // 更新当前步骤
                    currentStep += 2;

                    // 如果传入了sessionId，保存更新后的步骤
                    if (sessionId != null && !sessionId.isEmpty()) {
                        // 这里可以实现保存到Redis或其他会话存储
                    }
                } else if (initialPrompt != null && !initialPrompt.isEmpty()) {
                    // 如果没有找到下一个提示，但有初始提示，则使用初始提示
                    nextPrompt = initialPrompt;
                } else {
                    // 默认提示
                    nextPrompt = "Please continue the conversation...";
                }

                // 构建响应
                response.put("content", aiReply);
                response.put("prompt", nextPrompt);

            } else {
                // 预设对话模式
                // 获取当前用户应该回复的对话内容
                DialogueContent userDialogue = dialogueContentService.queryContentByScenarioIdAndOrderNum(scenarioId, 1);

                if (userDialogue != null) {
                    // 更新用户输入
                    userDialogue.setContent(userInput);
                    dialogueContentService.updateContent(userDialogue);

                    // 获取下一条AI回复
                    DialogueContent aiResponse = dialogueContentService.queryContentByScenarioIdAndOrderNum(scenarioId, 2);

                    // 获取下一个提示（如果有）
                    String nextPrompt = "";
                    DialogueContent nextContent = dialogueContentService.queryContentByScenarioIdAndOrderNum(scenarioId, 3);
                    if (nextContent != null) {
                        nextPrompt = nextContent.getPrompt();
                    }

                    // 构建响应
                    if (aiResponse != null) {
                        response.put("content", aiResponse.getContent());
                    } else {
                        response.put("content", "没有找到对应的AI回复内容");
                    }
                    response.put("prompt", nextPrompt);
                } else {
                    response.put("content", "没有找到对应的对话内容");
                    response.put("prompt", "");
                }
            }

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("处理对话失败：" + e.getMessage()));
        }
    }

    /**
     * 管理员：添加对话场景
     */
    @PostMapping("/scenarios")
    public ResponseEntity<ApiResponse<DialogueScenario>> addScenario(@RequestBody DialogueScenario scenario) {
        int result = dialogueScenarioService.addScenario(scenario);
        if (result > 0) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("对话场景添加成功", scenario));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("添加失败，请稍后重试"));
        }
    }

    /**
     * 管理员：更新对话场景
     */
    @PutMapping("/scenarios/{scenarioId}")
    public ResponseEntity<ApiResponse<String>> updateScenario(@PathVariable Integer scenarioId, @RequestBody DialogueScenario scenario) {
        // 确保scenarioId匹配
        if (!scenarioId.equals(scenario.getScenarioId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("场景ID不匹配"));
        }

        // 检查场景是否存在
        DialogueScenario existingScenario = dialogueScenarioService.queryScenarioById(scenarioId);
        if (existingScenario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("对话场景不存在"));
        }

        int result = dialogueScenarioService.updateScenario(scenario);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("对话场景更新成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新失败，请稍后重试"));
        }
    }

    /**
     * 管理员：删除对话场景
     */
    @DeleteMapping("/scenarios/{scenarioId}")
    public ResponseEntity<ApiResponse<String>> deleteScenario(@PathVariable Integer scenarioId) {
        // 检查场景是否存在
        DialogueScenario existingScenario = dialogueScenarioService.queryScenarioById(scenarioId);
        if (existingScenario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("对话场景不存在"));
        }

        int result = dialogueScenarioService.deleteScenario(scenarioId);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("对话场景删除成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除失败，请稍后重试"));
        }
    }

    /**
     * 重置对话
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetDialogue(
            @RequestParam Integer scenarioId,
            @RequestParam(required = false) String initialMessage,
            @RequestParam(required = false) String initialPrompt,
            @RequestParam(required = false) String sessionId) {

        Map<String, String> response = new HashMap<>();

        try {
            // 获取场景信息
            DialogueScenario scenario = dialogueScenarioService.queryScenarioById(scenarioId);
            if (scenario == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("对话场景不存在"));
            }

            // 创建新的对话历史
            DialogueHistory dialogueHistory = new DialogueHistory();
            dialogueHistory.setScenarioId(scenarioId);
            dialogueHistory.setScenarioName(scenario.getScenarioName());
            dialogueHistory.setScenarioDescription(scenario.getScenarioDescription());

            // 处理初始消息
            String contentToReturn = "";

            // 优先使用前端传来的初始消息
            if (initialMessage != null && !initialMessage.isEmpty()) {
                contentToReturn = initialMessage;
                dialogueHistory.addMessage(initialMessage);
            } else {
                // 如果前端没有传递初始消息，尝试从数据库获取
                DialogueContent dbInitialMessage = dialogueContentService.queryContentByScenarioIdAndOrderNum(scenarioId, 1);
                if (dbInitialMessage != null) {
                    contentToReturn = dbInitialMessage.getContent();
                    dialogueHistory.addMessage(contentToReturn);
                }
            }

            // 处理提示信息
            String promptToReturn = "";

            // 优先使用前端传来的提示
            if (initialPrompt != null && !initialPrompt.isEmpty()) {
                promptToReturn = initialPrompt;
            } else {
                // 如果前端没有传递提示，尝试从数据库获取
                // 重置当前对话步骤为1
                Integer currentStep = 1;

                // 如果传入了sessionId，保存步骤
                if (sessionId != null && !sessionId.isEmpty()) {
                    // 这里可以实现保存到Redis或其他会话存储
                }

                // 获取第二条内容的提示（如果有）
                DialogueContent nextContent = dialogueContentService.queryContentByScenarioIdAndOrderNum(scenarioId, 2);
                if (nextContent != null) {
                    promptToReturn = nextContent.getPrompt();
                } else {
                    promptToReturn = "Please start the conversation...";
                }
            }

            // 构建响应
            response.put("content", contentToReturn);
            response.put("prompt", promptToReturn);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("重置对话失败：" + e.getMessage()));
        }
    }

    /**
     * 切换AI模式
     */
    @PostMapping("/toggle-ai-mode")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleAiMode(
            @RequestParam(required = false) Boolean currentMode,
            @RequestParam(required = false) String sessionId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 切换模式
            Boolean newMode = (currentMode == null || !currentMode);

            // 构建响应
            response.put("useAiMode", newMode);
            response.put("message", newMode ? "已切换到AI对话模式" : "已切换到预设对话模式");

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("切换AI模式失败：" + e.getMessage()));
        }
    }
}
