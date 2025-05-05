package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.pojo.User;
import com.chun.myspringboot.pojo.UserActivity;
import com.chun.myspringboot.service.UserActivityService;
import com.chun.myspringboot.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户活动API控制器
 */
@Api(tags = "用户活动API")
@RestController
@RequestMapping("/api/users/{userId}/activities")
public class UserActivityApiController {
    
    @Autowired
    private UserActivityService userActivityService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取用户活动列表
     */
    @ApiOperation(value = "获取用户活动列表", notes = "获取指定用户的所有活动")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserActivity>>> getUserActivities(
            @ApiParam(value = "用户ID", required = true) @PathVariable Integer userId) {
        // 检查用户是否存在
        User user = userService.queryUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }
        
        List<UserActivity> activities = userActivityService.queryUserActivitiesByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
    
    /**
     * 获取用户最近活动
     */
    @ApiOperation(value = "获取用户最近活动", notes = "获取指定用户的最近活动")
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRecentUserActivities(
            @ApiParam(value = "用户ID", required = true) @PathVariable Integer userId,
            @ApiParam(value = "限制数量", required = false) @RequestParam(defaultValue = "5") Integer limit) {
        // 检查用户是否存在
        User user = userService.queryUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }
        
        List<UserActivity> activities = userActivityService.queryRecentUserActivitiesByUserId(userId, limit);
        
        // 转换为前端需要的格式
        List<Map<String, Object>> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        for (UserActivity activity : activities) {
            Map<String, Object> item = new HashMap<>();
            item.put("content", activity.getActivityContent());
            item.put("time", sdf.format(activity.getActivityTime()));
            
            // 根据活动类型设置不同的类型标识
            String type = "info";
            switch (activity.getActivityType()) {
                case "word_study":
                case "listening":
                    type = "success";
                    break;
                case "exam":
                    type = "primary";
                    break;
                case "essay":
                    type = "info";
                    break;
                case "reading":
                    type = "warning";
                    break;
                case "pronunciation":
                    type = "danger";
                    break;
            }
            item.put("type", type);
            
            result.add(item);
        }
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 添加用户活动
     */
    @ApiOperation(value = "添加用户活动", notes = "添加用户活动记录")
    @PostMapping
    public ResponseEntity<ApiResponse<UserActivity>> addUserActivity(
            @ApiParam(value = "用户ID", required = true) @PathVariable Integer userId,
            @ApiParam(value = "用户活动信息", required = true) @RequestBody UserActivity userActivity) {
        // 检查用户是否存在
        User user = userService.queryUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }
        
        // 设置用户ID
        userActivity.setUserId(userId);
        
        int result = userActivityService.addUserActivity(userActivity);
        if (result > 0) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("活动记录添加成功", userActivity));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("添加失败，请稍后重试"));
        }
    }
}
