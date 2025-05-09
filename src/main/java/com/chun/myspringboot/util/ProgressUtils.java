package com.chun.myspringboot.util;

import com.chun.myspringboot.pojo.Word;
import com.chun.myspringboot.service.Impl.WordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 调用DataUtils处理数据生成百分比
 * 前后端分离后，返回数据而不是Model
 */
@Component
public class ProgressUtils {
   @Autowired
   private WordServiceImpl wordService;

   @Autowired
   private DataUtils dataUtils;

   /**
    * 获取学习进度数据
    * @param grade 年级
    * @return 包含进度百分比的Map
    */
   public Map<String, String> getProgress(Integer grade) {
        int remember = wordService.queryRememberNumberByGrade(grade);
        int total = wordService.queryAllWordNumberByGrade(grade);
        int study = wordService.queryStudyNumberByGrade(grade);

        String studyPercent = dataUtils.percent(study, total);
        String rememberPercent = dataUtils.percent(remember, total);
        String unrememberPercent = dataUtils.unpercent(remember, total);

        Map<String, String> progressData = new HashMap<>();
        progressData.put("remember", rememberPercent);
        progressData.put("unremembered", unrememberPercent);
        progressData.put("study", studyPercent);

        return progressData;
    }

    /**
     * 获取单词收藏状态
     * @param word 单词对象
     * @return 收藏状态
     */
    public String getCollectionStatus(Word word) {
        Integer collection = word.getCollection();
        return collection == 0 ? "加入收藏" : "已经收藏";
    }
}
