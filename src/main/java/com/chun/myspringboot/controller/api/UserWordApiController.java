package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.pojo.User;
import com.chun.myspringboot.pojo.UserWord;
import com.chun.myspringboot.pojo.Word;
import com.chun.myspringboot.service.ActivityRecordService;
import com.chun.myspringboot.service.UserService;
import com.chun.myspringboot.service.UserWordService;
import com.chun.myspringboot.service.WordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户单词关联API控制器
 */
@Api(tags = "用户单词关联API")
@RestController
@RequestMapping("/api/users/{userId}/words")
public class UserWordApiController {

    @Autowired
    private UserWordService userWordService;

    @Autowired
    private WordService wordService;

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityRecordService activityRecordService;

    /**
     * 获取用户单词关联列表
     */
    @ApiOperation(value = "获取用户单词关联列表", notes = "获取用户的单词关联列表")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserWord>>> getUserWords(
            @ApiParam(value = "用户ID", required = true) @PathVariable Integer userId) {
        // 检查用户是否存在
        User user = userService.queryUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }

        List<UserWord> userWords = userWordService.queryUserWordsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(userWords));
    }

    /**
     * 获取用户单词关联详情
     */
    @ApiOperation(value = "获取用户单词关联详情", notes = "获取用户单词关联详情")
    @GetMapping("/{wordId}")
    public ResponseEntity<ApiResponse<UserWord>> getUserWord(
            @ApiParam(value = "用户ID", required = true) @PathVariable Integer userId,
            @ApiParam(value = "单词ID", required = true) @PathVariable Integer wordId) {
        // 检查用户是否存在
        User user = userService.queryUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }

        // 检查单词是否存在
        Word word = wordService.queryWordById(wordId);
        if (word == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("单词不存在"));
        }

        UserWord userWord = userWordService.queryUserWordByUserIdAndWordId(userId, wordId);
        if (userWord != null) {
            return ResponseEntity.ok(ApiResponse.success(userWord));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户单词关联不存在"));
        }
    }

    /**
     * 标记单词为已学习
     */
    @ApiOperation(value = "标记单词为已学习", notes = "标记单词为已学习")
    @PostMapping("/{wordId}/study")
    public ResponseEntity<ApiResponse<Map<String, Object>>> studyWord(
            @ApiParam(value = "用户ID", required = true) @PathVariable Integer userId,
            @ApiParam(value = "单词ID", required = true) @PathVariable Integer wordId) {
        // 检查用户是否存在
        User user = userService.queryUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }

        // 检查单词是否存在
        Word word = wordService.queryWordById(wordId);
        if (word == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("单词不存在"));
        }

        // 查询用户单词关联
        UserWord userWord = userWordService.queryUserWordByUserIdAndWordId(userId, wordId);

        // 如果不存在，创建新的关联
        if (userWord == null) {
            userWord = new UserWord();
            userWord.setUserId(userId);
            userWord.setWordId(wordId);
            userWord.setStudy(1);
            userWord.setRemember(0);
            userWord.setCollection(0);
            userWord.setLearnTime(new Date());

            userWordService.addUserWord(userWord);

            // 记录单词学习活动
            activityRecordService.recordWordStudyActivity(userId, wordId, word.getWordName());
        } else {
            // 如果已存在，更新学习状态
            userWord.setStudy(1);
            userWord.setLearnTime(new Date());

            // 如果之前未学习，记录活动
            if (userWord.getStudy() == null || userWord.getStudy() == 0) {
                activityRecordService.recordWordStudyActivity(userId, wordId, word.getWordName());
            }

            userWordService.updateUserWord(userWord);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userWord", userWord);
        result.put("message", "单词已标记为已学习");

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 标记单词为已掌握
     */
    @ApiOperation(value = "标记单词为已掌握", notes = "标记单词为已掌握")
    @PostMapping("/{wordId}/remember")
    public ResponseEntity<ApiResponse<Map<String, Object>>> rememberWord(
            @ApiParam(value = "用户ID", required = true) @PathVariable Integer userId,
            @ApiParam(value = "单词ID", required = true) @PathVariable Integer wordId) {
        // 检查用户是否存在
        User user = userService.queryUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }

        // 检查单词是否存在
        Word word = wordService.queryWordById(wordId);
        if (word == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("单词不存在"));
        }

        // 查询用户单词关联
        UserWord userWord = userWordService.queryUserWordByUserIdAndWordId(userId, wordId);

        // 如果不存在，创建新的关联
        if (userWord == null) {
            userWord = new UserWord();
            userWord.setUserId(userId);
            userWord.setWordId(wordId);
            userWord.setStudy(1);
            userWord.setRemember(1);
            userWord.setCollection(0);
            userWord.setLearnTime(new Date());

            userWordService.addUserWord(userWord);

            // 记录单词掌握活动
            String content = "掌握了单词 「" + word.getWordName() + "」";
            activityRecordService.recordActivity(userId, "word_remember", content, wordId);
        } else {
            // 如果已存在，更新记忆状态
            boolean needRecord = userWord.getRemember() == null || userWord.getRemember() == 0;

            userWord.setStudy(1);
            userWord.setRemember(1);
            userWord.setLearnTime(new Date());

            userWordService.updateUserWord(userWord);

            // 如果之前未掌握，记录活动
            if (needRecord) {
                String content = "掌握了单词 「" + word.getWordName() + "」";
                activityRecordService.recordActivity(userId, "word_remember", content, wordId);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userWord", userWord);
        result.put("message", "单词已标记为已掌握");

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 标记单词为未掌握
     */
    @ApiOperation(value = "标记单词为未掌握", notes = "标记单词为未掌握")
    @PostMapping("/{wordId}/forget")
    public ResponseEntity<ApiResponse<Map<String, Object>>> forgetWord(
            @ApiParam(value = "用户ID", required = true) @PathVariable Integer userId,
            @ApiParam(value = "单词ID", required = true) @PathVariable Integer wordId) {
        // 检查用户是否存在
        User user = userService.queryUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }

        // 检查单词是否存在
        Word word = wordService.queryWordById(wordId);
        if (word == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("单词不存在"));
        }

        // 查询用户单词关联
        UserWord userWord = userWordService.queryUserWordByUserIdAndWordId(userId, wordId);

        // 如果不存在，创建新的关联
        if (userWord == null) {
            userWord = new UserWord();
            userWord.setUserId(userId);
            userWord.setWordId(wordId);
            userWord.setStudy(1);
            userWord.setRemember(0);
            userWord.setCollection(0);
            userWord.setLearnTime(new Date());

            userWordService.addUserWord(userWord);

            // 记录单词未掌握活动
            String content = "将单词 「" + word.getWordName() + "」 标记为未掌握";
            activityRecordService.recordActivity(userId, "word_forget", content, wordId);
        } else {
            // 如果已存在，更新记忆状态
            boolean needRecord = userWord.getRemember() != null && userWord.getRemember() == 1;

            userWord.setRemember(0);
            userWord.setLearnTime(new Date());

            userWordService.updateUserWord(userWord);

            // 如果之前已掌握，记录活动
            if (needRecord) {
                String content = "将单词 「" + word.getWordName() + "」 标记为未掌握";
                activityRecordService.recordActivity(userId, "word_forget", content, wordId);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userWord", userWord);
        result.put("message", "单词已标记为未掌握");

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 收藏/取消收藏单词
     */
    @ApiOperation(value = "收藏/取消收藏单词", notes = "收藏/取消收藏单词")
    @PostMapping("/{wordId}/collection")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleCollection(
            @ApiParam(value = "用户ID", required = true) @PathVariable Integer userId,
            @ApiParam(value = "单词ID", required = true) @PathVariable Integer wordId) {
        // 检查用户是否存在
        User user = userService.queryUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }

        // 检查单词是否存在
        Word word = wordService.queryWordById(wordId);
        if (word == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("单词不存在"));
        }

        // 查询用户单词关联
        UserWord userWord = userWordService.queryUserWordByUserIdAndWordId(userId, wordId);

        Map<String, Object> result = new HashMap<>();

        // 如果不存在，创建新的关联并收藏
        if (userWord == null) {
            userWord = new UserWord();
            userWord.setUserId(userId);
            userWord.setWordId(wordId);
            userWord.setStudy(0);
            userWord.setRemember(0);
            userWord.setCollection(1);
            userWord.setLearnTime(new Date());

            userWordService.addUserWord(userWord);

            // 记录收藏活动
            String content = "收藏了单词 「" + word.getWordName() + "」";
            activityRecordService.recordActivity(userId, "word_collection", content, wordId);

            result.put("collected", true);
            result.put("message", "单词已收藏");
        } else {
            // 如果已存在，切换收藏状态
            if (userWord.getCollection() == 0) {
                userWord.setCollection(1);

                // 记录收藏活动
                String content = "收藏了单词 「" + word.getWordName() + "」";
                activityRecordService.recordActivity(userId, "word_collection", content, wordId);

                result.put("collected", true);
                result.put("message", "单词已收藏");
            } else {
                userWord.setCollection(0);

                // 记录取消收藏活动
                String content = "取消收藏单词 「" + word.getWordName() + "」";
                activityRecordService.recordActivity(userId, "word_uncollection", content, wordId);

                result.put("collected", false);
                result.put("message", "已取消收藏");
            }

            userWordService.updateUserWord(userWord);
        }

        result.put("userWord", userWord);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
