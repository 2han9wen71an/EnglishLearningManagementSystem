package com.chun.myspringboot.controller;

import com.chun.myspringboot.pojo.PronunciationAssessment;
import com.chun.myspringboot.pojo.User;
import com.chun.myspringboot.service.Impl.PronunciationAssessmentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PronunciationController {
    
    @Autowired
    private PronunciationAssessmentServiceImpl pronunciationAssessmentService;
    
    // 查看语音评测页面
    @RequestMapping("/viewPronunciation")
    public String viewPronunciation() {
        return "user/pronunciation/pronunciation-assessment";
    }
    
    // 查看历史评测记录
    @RequestMapping("/viewPronunciationHistory")
    public String viewPronunciationHistory(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/";
        }
        
        List<PronunciationAssessment> assessments = pronunciationAssessmentService.queryAssessmentsByUserId(user.getUserId());
        model.addAttribute("assessments", assessments);
        return "user/pronunciation/pronunciation-history";
    }
    
    // 查看评测详情
    @RequestMapping("/viewAssessment/{assessmentId}")
    public String viewAssessment(@PathVariable("assessmentId") Integer assessmentId, Model model) {
        PronunciationAssessment assessment = pronunciationAssessmentService.queryAssessmentById(assessmentId);
        model.addAttribute("assessment", assessment);
        return "user/pronunciation/assessment-detail";
    }
    
    // 提交语音评测
    @PostMapping("/submitPronunciation")
    @ResponseBody
    public Map<String, Object> submitPronunciation(
            @RequestParam("content") String content,
            @RequestParam("audioData") String audioData,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        
        try {
            PronunciationAssessment assessment = pronunciationAssessmentService.assessPronunciation(
                    user.getUserId(), content, audioData);
            
            if (assessment != null) {
                result.put("success", true);
                result.put("message", "评测成功");
                result.put("assessmentId", assessment.getAssessmentId());
                result.put("phonemeErrors", assessment.getPhonemeErrors());
                result.put("overallScore", assessment.getOverallScore());
            } else {
                result.put("success", false);
                result.put("message", "评测失败，请稍后重试");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    // 删除评测记录
    @PostMapping("/deleteAssessment")
    @ResponseBody
    public Map<String, Object> deleteAssessment(@RequestParam("assessmentId") Integer assessmentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int count = pronunciationAssessmentService.deleteAssessment(assessmentId);
            if (count > 0) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "删除失败，请稍后重试");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    // 管理员查看所有评测记录
    @RequestMapping("/admin/viewAllAssessments")
    public String viewAllAssessments(Model model) {
        List<PronunciationAssessment> assessments = pronunciationAssessmentService.queryAllAssessments();
        model.addAttribute("assessments", assessments);
        return "admin/pronunciation-list";
    }
}
