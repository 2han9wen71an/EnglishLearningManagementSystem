package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.pojo.Listen;
import com.chun.myspringboot.service.Impl.ListenServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 听力API控制器
 */
@RestController
@RequestMapping("/api/listens")
public class ListenApiController {
    
    @Autowired
    private ListenServiceImpl listenService;
    
    /**
     * 获取所有听力
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Listen>>> getAllListens() {
        List<Listen> listens = listenService.queryAllListen();
        return ResponseEntity.ok(ApiResponse.success(listens));
    }
    
    /**
     * 获取听力详情
     */
    @GetMapping("/{listenId}")
    public ResponseEntity<ApiResponse<Listen>> getListenById(@PathVariable Integer listenId) {
        Listen listen = listenService.queryListenById(listenId);
        if (listen != null) {
            return ResponseEntity.ok(ApiResponse.success(listen));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("听力不存在"));
        }
    }
    
    /**
     * 管理员：添加听力
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Listen>> addListen(@RequestBody Listen listen) {
        // 这里需要添加一个新的方法来添加听力
        // 假设已经存在这个方法
        int result = listenService.addListen(listen);
        if (result > 0) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("听力添加成功", listen));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("添加失败，请稍后重试"));
        }
    }
    
    /**
     * 管理员：更新听力
     */
    @PutMapping("/{listenId}")
    public ResponseEntity<ApiResponse<String>> updateListen(@PathVariable Integer listenId, @RequestBody Listen listen) {
        // 确保listenId匹配
        if (!listenId.equals(listen.getListenId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("听力ID不匹配"));
        }
        
        // 检查听力是否存在
        Listen existingListen = listenService.queryListenById(listenId);
        if (existingListen == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("听力不存在"));
        }
        
        // 这里需要添加一个新的方法来更新听力
        // 假设已经存在这个方法
        int result = listenService.updateListen(listen);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("听力更新成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新失败，请稍后重试"));
        }
    }
    
    /**
     * 管理员：删除听力
     */
    @DeleteMapping("/{listenId}")
    public ResponseEntity<ApiResponse<String>> deleteListen(@PathVariable Integer listenId) {
        // 检查听力是否存在
        Listen existingListen = listenService.queryListenById(listenId);
        if (existingListen == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("听力不存在"));
        }
        
        // 这里需要添加一个新的方法来删除听力
        // 假设已经存在这个方法
        int result = listenService.deleteListen(listenId);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("听力删除成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除失败，请稍后重试"));
        }
    }
}
