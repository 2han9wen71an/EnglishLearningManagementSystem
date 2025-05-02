package com.chun.myspringboot.controller;

import com.chun.myspringboot.pojo.User;
import com.chun.myspringboot.pojo.WordCard;
import com.chun.myspringboot.service.Impl.WordCardServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WordCardController {

    @Autowired
    private WordCardServiceImpl wordCardService;

    /**
     * 跳转到单词卡片生成页面
     */
    @RequestMapping("/toWordCard")
    public String toWordCard() {
        return "user/word/word-card";
    }

    /**
     * 生成单词卡片
     * 接收AJAX请求，返回JSON数据
     */
    @PostMapping("/generateWordCard")
    @ResponseBody
    public Map<String, Object> generateWordCard(@RequestParam("word") String word, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取当前登录用户
            User loginUser = (User) session.getAttribute("loginUser");
            
            // 生成单词卡片
            WordCard wordCard = wordCardService.generateWordCard(word);
            
            // 如果用户已登录，关联用户ID
            if (loginUser != null && wordCard.getUserId() == null) {
                wordCard.setUserId(loginUser.getUserId());
                wordCardService.saveWordCard(wordCard);
            }
            
            // 返回成功响应
            response.put("success", true);
            response.put("wordCard", wordCard);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回错误响应
            response.put("success", false);
            response.put("message", "生成单词卡片失败：" + e.getMessage());
        }
        
        return response;
    }

    /**
     * 查看单词卡片历史
     */
    @RequestMapping("/wordCardHistory")
    public String wordCardHistory(Model model, HttpSession session) {
        // 获取当前登录用户
        User loginUser = (User) session.getAttribute("loginUser");
        
        if (loginUser != null) {
            // 查询用户的单词卡片历史
            model.addAttribute("wordCards", wordCardService.queryWordCardsByUserId(loginUser.getUserId()));
        }
        
        return "user/word/word-card-history";
    }
}
