package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.pojo.Grade;
import com.chun.myspringboot.pojo.Word;
import com.chun.myspringboot.service.Impl.GradeServiceImpl;
import com.chun.myspringboot.service.Impl.WordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单词API控制器
 */
@RestController
@RequestMapping("/api/words")
public class WordApiController {

    @Autowired
    private WordServiceImpl wordService;

    @Autowired
    private GradeServiceImpl gradeService;

    /**
     * 获取所有单词
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Word>>> getAllWords(@RequestParam(required = false) Integer gradeId) {
        List<Word> words;
        if (gradeId != null) {
            // 由于没有直接按等级查询的方法，我们可以先获取所有单词，然后在前端过滤
            words = wordService.queryAllWord();
        } else {
            words = wordService.queryAllWord();
        }
        return ResponseEntity.ok(ApiResponse.success(words));
    }

    /**
     * 获取单词详情
     */
    @GetMapping("/{wordId}")
    public ResponseEntity<ApiResponse<Word>> getWordById(@PathVariable Integer wordId) {
        Word word = wordService.queryWordById(wordId);
        if (word != null) {
            return ResponseEntity.ok(ApiResponse.success(word));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("单词不存在"));
        }
    }

    /**
     * 获取学习进度统计
     */
    @GetMapping("/progress")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWordProgress() {
        Map<String, Object> progress = new HashMap<>();

        List<Grade> grades = gradeService.queryAllGrade();
        for (Grade grade : grades) {
            Map<String, Object> gradeProgress = new HashMap<>();
            int rememberCount = wordService.queryRememberNumberByGrade(grade.getGradeId());
            int totalCount = wordService.queryAllWordNumberByGrade(grade.getGradeId());
            int studyCount = wordService.queryStudyNumberByGrade(grade.getGradeId());

            gradeProgress.put("rememberCount", rememberCount);
            gradeProgress.put("totalCount", totalCount);
            gradeProgress.put("studyCount", studyCount);
            gradeProgress.put("rememberPercent", (totalCount > 0) ? (rememberCount * 100.0 / totalCount) : 0);
            gradeProgress.put("studyPercent", (totalCount > 0) ? (studyCount * 100.0 / totalCount) : 0);

            progress.put(grade.getGradeName(), gradeProgress);
        }

        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    /**
     * 获取下一个学习单词
     */
    @GetMapping("/next/{gradeId}")
    public ResponseEntity<ApiResponse<Word>> getNextWord(@PathVariable Integer gradeId) {
        Word word = wordService.queryWordStudy0ByGrade(gradeId);
        if (word != null) {
            return ResponseEntity.ok(ApiResponse.success(word));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("已完成所有单词学习"));
        }
    }

    /**
     * 标记单词为已记住
     */
    @PostMapping("/{wordId}/remember")
    public ResponseEntity<ApiResponse<String>> rememberWord(@PathVariable Integer wordId) {
        // 检查单词是否存在
        Word existingWord = wordService.queryWordById(wordId);
        if (existingWord == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("单词不存在"));
        }

        // 标记为已记住
        int result = wordService.updateWordRemember1(wordId);
        // 更新学习记录
        wordService.updateWordStudy1(wordId);

        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("单词已标记为记住", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("操作失败，请稍后重试"));
        }
    }

    /**
     * 标记单词为未记住
     */
    @PostMapping("/{wordId}/forget")
    public ResponseEntity<ApiResponse<String>> forgetWord(@PathVariable Integer wordId) {
        // 检查单词是否存在
        Word existingWord = wordService.queryWordById(wordId);
        if (existingWord == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("单词不存在"));
        }

        // 标记为未记住
        int result = wordService.updateWordRemember0(wordId);

        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("单词已标记为未记住", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("操作失败，请稍后重试"));
        }
    }

    /**
     * 收藏/取消收藏单词
     */
    @PostMapping("/{wordId}/collection")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleCollection(@PathVariable Integer wordId) {
        // 检查单词是否存在
        Word existingWord = wordService.queryWordById(wordId);
        if (existingWord == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("单词不存在"));
        }

        Map<String, Object> result = new HashMap<>();

        // 切换收藏状态
        if (existingWord.getCollection() == 0) {
            // 收藏单词
            wordService.updateWordCollection1(wordId);
            result.put("collected", true);
            result.put("message", "单词已收藏");
        } else {
            // 取消收藏
            wordService.updateWordCollection0(wordId);
            result.put("collected", false);
            result.put("message", "已取消收藏");
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取收藏的单词
     */
    @GetMapping("/collection")
    public ResponseEntity<ApiResponse<List<Word>>> getCollectionWords() {
        List<Word> words = wordService.queryAllWordCollection();
        return ResponseEntity.ok(ApiResponse.success(words));
    }

    /**
     * 获取生词本（未记住的单词）
     */
    @GetMapping("/unremembered")
    public ResponseEntity<ApiResponse<List<Word>>> getUnrememberedWords() {
        List<Word> words = wordService.queryAllUnremembered();
        return ResponseEntity.ok(ApiResponse.success(words));
    }

    /**
     * 重置学习进度
     */
    @PostMapping("/reset/{gradeId}")
    public ResponseEntity<ApiResponse<String>> resetWordProgress(@PathVariable Integer gradeId) {
        int result = wordService.updateWordStudyByGrade(gradeId);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("学习进度已重置", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("重置失败，请稍后重试"));
        }
    }

    /**
     * 管理员：添加单词
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Word>> addWord(@RequestBody Word word) {
        int result = wordService.addWord(word);
        if (result > 0) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("单词添加成功", word));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("添加失败，请稍后重试"));
        }
    }

    /**
     * 管理员：更新单词
     */
    @PutMapping("/{wordId}")
    public ResponseEntity<ApiResponse<String>> updateWord(@PathVariable Integer wordId, @RequestBody Word word) {
        // 确保wordId匹配
        if (!wordId.equals(word.getWordId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("单词ID不匹配"));
        }

        // 检查单词是否存在
        Word existingWord = wordService.queryWordById(wordId);
        if (existingWord == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("单词不存在"));
        }

        int result = wordService.updateWord(word);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("单词更新成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新失败，请稍后重试"));
        }
    }

    /**
     * 管理员：删除单词
     */
    @DeleteMapping("/{wordId}")
    public ResponseEntity<ApiResponse<String>> deleteWord(@PathVariable Integer wordId) {
        // 检查单词是否存在
        Word existingWord = wordService.queryWordById(wordId);
        if (existingWord == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("单词不存在"));
        }

        int result = wordService.deleteWord(wordId);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("单词删除成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除失败，请稍后重试"));
        }
    }
}
