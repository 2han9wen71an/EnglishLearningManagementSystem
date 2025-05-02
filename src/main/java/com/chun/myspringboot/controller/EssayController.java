package com.chun.myspringboot.controller;

import com.chun.myspringboot.pojo.Essay;
import com.chun.myspringboot.pojo.EssayCorrection;
import com.chun.myspringboot.pojo.User;
import com.chun.myspringboot.service.Impl.EssayCorrectionServiceImpl;
import com.chun.myspringboot.service.Impl.EssayServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EssayController {
    
    @Autowired
    private EssayServiceImpl essayService;
    
    @Autowired
    private EssayCorrectionServiceImpl essayCorrectionService;
    
    // 查看所有作文
    @RequestMapping("/viewEssays")
    public String viewEssays(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/";
        }
        
        List<Essay> essays = essayService.queryEssaysByUserId(user.getUserId());
        model.addAttribute("essays", essays);
        return "user/essay/essay-list";
    }
    
    // 去写作文页面
    @RequestMapping("/toWriteEssay")
    public String toWriteEssay() {
        return "user/essay/write-essay";
    }
    
    // 提交作文
    @PostMapping("/submitEssay")
    public String submitEssay(Essay essay, HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/";
        }
        
        essay.setUserId(user.getUserId());
        essay.setSubmitTime(new Date());
        essay.setStatus(0); // 未批改
        
        essayService.addEssay(essay);
        
        return "redirect:/viewEssays";
    }
    
    // 查看作文详情
    @RequestMapping("/viewEssay/{essayId}")
    public String viewEssay(@PathVariable("essayId") Integer essayId, Model model) {
        Essay essay = essayService.queryEssayById(essayId);
        model.addAttribute("essay", essay);
        
        // 查询是否有批改结果
        EssayCorrection correction = essayCorrectionService.queryCorrectionByEssayId(essayId);
        model.addAttribute("correction", correction);
        
        return "user/essay/essay-detail";
    }
    
    // 请求AI批改
    @PostMapping("/correctEssay")
    @ResponseBody
    public Map<String, Object> correctEssay(@RequestParam("essayId") Integer essayId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            EssayCorrection correction = essayCorrectionService.correctEssayWithAI(essayId);
            if (correction != null) {
                result.put("success", true);
                result.put("message", "批改成功");
                result.put("correctionId", correction.getCorrectionId());
            } else {
                result.put("success", false);
                result.put("message", "批改失败，请稍后重试");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    // 管理员查看所有作文
    @RequestMapping("/admin/viewAllEssays")
    public String viewAllEssays(Model model) {
        List<Essay> essays = essayService.queryAllEssays();
        model.addAttribute("essays", essays);
        return "admin/essay-list";
    }
    
    // 删除作文
    @PostMapping("/deleteEssay")
    @ResponseBody
    public Map<String, Object> deleteEssay(@RequestParam("essayId") Integer essayId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int count = essayService.deleteEssay(essayId);
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
}
