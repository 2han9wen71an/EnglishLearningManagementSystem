package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.pojo.Notice;
import com.chun.myspringboot.service.Impl.NoticeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告API控制器
 */
@RestController
@RequestMapping("/api/notices")
public class NoticeApiController {

    @Autowired
    private NoticeServiceImpl noticeService;

    /**
     * 获取所有公告
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Notice>>> getAllNotices() {
        List<Notice> notices = noticeService.queryAllNotice();
        return ResponseEntity.ok(ApiResponse.success(notices));
    }

    /**
     * 获取最新公告
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<Notice>> getLatestNotice() {
        Notice notice = noticeService.queryNewNoticeById();
        if (notice != null) {
            return ResponseEntity.ok(ApiResponse.success(notice));
        } else {
            // 如果没有公告，创建一个默认公告
            Notice defaultNotice = new Notice();
            defaultNotice.setTitle("欢迎使用英语知识应用网站系统");
            defaultNotice.setContent("这是一个默认公告。管理员尚未发布任何公告。");
            defaultNotice.setCreatTime(new java.util.Date());

            return ResponseEntity.ok(ApiResponse.success(defaultNotice));
        }
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<Notice>> getNoticeById(@PathVariable Integer noticeId) {
        Notice notice = noticeService.queryNoticeById(noticeId);
        if (notice != null) {
            return ResponseEntity.ok(ApiResponse.success(notice));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("公告不存在"));
        }
    }

    /**
     * 管理员：添加公告
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Notice>> addNotice(@RequestBody Notice notice) {
        // 设置创建时间
        if (notice.getCreatTime() == null) {
            notice.setCreatTime(new java.util.Date());
        }

        // 这里需要添加一个新的方法来添加公告
        // 假设已经存在这个方法
        int result = noticeService.addNotice(notice);
        if (result > 0) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("公告添加成功", notice));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("添加失败，请稍后重试"));
        }
    }

    /**
     * 管理员：更新公告
     */
    @PutMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<String>> updateNotice(@PathVariable Integer noticeId, @RequestBody Notice notice) {
        // 确保noticeId匹配
        if (!noticeId.equals(notice.getNoticeId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("公告ID不匹配"));
        }

        // 检查公告是否存在
        Notice existingNotice = noticeService.queryNoticeById(noticeId);
        if (existingNotice == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("公告不存在"));
        }

        // 这里需要添加一个新的方法来更新公告
        // 假设已经存在这个方法
        int result = noticeService.updateNotice(notice);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("公告更新成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新失败，请稍后重试"));
        }
    }

    /**
     * 管理员：删除公告
     */
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<String>> deleteNotice(@PathVariable Integer noticeId) {
        // 检查公告是否存在
        Notice existingNotice = noticeService.queryNoticeById(noticeId);
        if (existingNotice == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("公告不存在"));
        }

        // 这里需要添加一个新的方法来删除公告
        // 假设已经存在这个方法
        int result = noticeService.deleteNotice(noticeId);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("公告删除成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除失败，请稍后重试"));
        }
    }
}
