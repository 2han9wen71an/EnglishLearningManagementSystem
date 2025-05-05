package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.pojo.Essay;
import com.chun.myspringboot.pojo.EssayCorrection;
import com.chun.myspringboot.service.Impl.EssayCorrectionServiceImpl;
import com.chun.myspringboot.service.Impl.EssayServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 作文API控制器
 */
@RestController
@RequestMapping("/api/essays")
public class EssayApiController {
    
    @Autowired
    private EssayServiceImpl essayService;
    
    @Autowired
    private EssayCorrectionServiceImpl essayCorrectionService;
    
    /**
     * 获取所有作文
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Essay>>> getAllEssays(@RequestParam(required = false) Integer userId) {
        List<Essay> essays;
        if (userId != null) {
            essays = essayService.queryEssaysByUserId(userId);
        } else {
            essays = essayService.queryAllEssays();
        }
        return ResponseEntity.ok(ApiResponse.success(essays));
    }
    
    /**
     * 获取作文详情
     */
    @GetMapping("/{essayId}")
    public ResponseEntity<ApiResponse<Essay>> getEssayById(@PathVariable Integer essayId) {
        Essay essay = essayService.queryEssayById(essayId);
        if (essay != null) {
            return ResponseEntity.ok(ApiResponse.success(essay));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("作文不存在"));
        }
    }
    
    /**
     * 提交作文
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Essay>> submitEssay(@RequestBody Essay essay) {
        // 设置提交时间和状态
        essay.setSubmitTime(new Date());
        essay.setStatus(0); // 未批改
        
        int result = essayService.addEssay(essay);
        if (result > 0) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("作文提交成功", essay));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("提交失败，请稍后重试"));
        }
    }
    
    /**
     * 更新作文
     */
    @PutMapping("/{essayId}")
    public ResponseEntity<ApiResponse<String>> updateEssay(@PathVariable Integer essayId, @RequestBody Essay essay) {
        // 确保essayId匹配
        if (!essayId.equals(essay.getEssayId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("作文ID不匹配"));
        }
        
        // 检查作文是否存在
        Essay existingEssay = essayService.queryEssayById(essayId);
        if (existingEssay == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("作文不存在"));
        }
        
        int result = essayService.updateEssay(essay);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("作文更新成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新失败，请稍后重试"));
        }
    }
    
    /**
     * 删除作文
     */
    @DeleteMapping("/{essayId}")
    public ResponseEntity<ApiResponse<String>> deleteEssay(@PathVariable Integer essayId) {
        // 检查作文是否存在
        Essay existingEssay = essayService.queryEssayById(essayId);
        if (existingEssay == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("作文不存在"));
        }
        
        int result = essayService.deleteEssay(essayId);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("作文删除成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除失败，请稍后重试"));
        }
    }
    
    /**
     * 请求AI批改
     */
    @PostMapping("/{essayId}/correct")
    public ResponseEntity<ApiResponse<EssayCorrection>> correctEssay(@PathVariable Integer essayId) {
        try {
            // 检查作文是否存在
            Essay existingEssay = essayService.queryEssayById(essayId);
            if (existingEssay == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("作文不存在"));
            }
            
            // 检查是否已有批改结果
            EssayCorrection existingCorrection = essayCorrectionService.queryCorrectionByEssayId(essayId);
            if (existingCorrection != null) {
                return ResponseEntity.ok(ApiResponse.success("作文已批改", existingCorrection));
            }
            
            // 使用AI批改作文
            EssayCorrection correction = essayCorrectionService.correctEssayWithAI(essayId);
            if (correction != null) {
                return ResponseEntity.ok(ApiResponse.success("批改成功", correction));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("批改失败，请稍后重试"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("系统错误：" + e.getMessage()));
        }
    }
    
    /**
     * 获取作文批改结果
     */
    @GetMapping("/{essayId}/correction")
    public ResponseEntity<ApiResponse<EssayCorrection>> getEssayCorrection(@PathVariable Integer essayId) {
        // 检查作文是否存在
        Essay existingEssay = essayService.queryEssayById(essayId);
        if (existingEssay == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("作文不存在"));
        }
        
        // 获取批改结果
        EssayCorrection correction = essayCorrectionService.queryCorrectionByEssayId(essayId);
        if (correction != null) {
            return ResponseEntity.ok(ApiResponse.success(correction));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("该作文尚未批改"));
        }
    }
}
