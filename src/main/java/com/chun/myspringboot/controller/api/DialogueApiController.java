package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
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
            @RequestParam(required = false, defaultValue = "true") Boolean useAiMode) {

        Map<String, String> response = new HashMap<>();

        try {
            // 获取场景信息
            DialogueScenario scenario = dialogueScenarioService.queryScenarioById(scenarioId);
            if (scenario == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("对话场景不存在"));
            }

            if (useAiMode) {
                // 使用AI模式生成回复
                String aiResponse = deepSeekService.generateDialogueResponse(
                        scenario.getScenarioName(),
                        scenario.getScenarioDescription(),
                        null, // 这里应该传入对话历史，但为简化处理暂时传null
                        userInput
                );

                response.put("content", aiResponse);
                response.put("prompt", "请继续对话...");
            } else {
                // 使用预设对话内容
                // 由于没有现成的方法，我们创建一个简单的响应
                response.put("content", "这是一个预设的回复。在实际实现中，这里应该返回根据用户输入匹配的下一条对话内容。");
                response.put("prompt", "请继续对话...");
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
}
