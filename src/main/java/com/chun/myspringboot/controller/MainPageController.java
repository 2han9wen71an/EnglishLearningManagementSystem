package com.chun.myspringboot.controller;

import com.chun.myspringboot.pojo.Notice;
import com.chun.myspringboot.pojo.Sentence;
import com.chun.myspringboot.pojo.User;
import com.chun.myspringboot.service.Impl.NoticeServiceImpl;
import com.chun.myspringboot.service.Impl.SentenceServiceImpl;
import com.chun.myspringboot.service.Impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpSession;
import java.util.Date;

@Controller
public class MainPageController {

    @Autowired
    private NoticeServiceImpl noticeService;
    @Autowired
    private SentenceServiceImpl sentenceService;
        //进入用户主页
        @RequestMapping("/main.html")
        public String toMainPage(Model model, HttpSession session){

            //查询最新公告
            Notice notice = noticeService.queryNewNoticeById();

            // 如果没有公告，创建一个默认公告
            if (notice == null) {
                notice = new Notice();
                notice.setTitle("欢迎使用英语学习管理系统");
                notice.setContent("这是一个默认公告。管理员尚未发布任何公告。");
                notice.setCreatTime(new Date());
            }

            model.addAttribute("notice", notice);

            //随机查询每日一句
            Sentence sentence = sentenceService.queryRandomSentence();

            // 如果没有句子，创建一个默认句子
            if (sentence == null) {
                sentence = new Sentence();
                sentence.setSentenceName("The best preparation for tomorrow is doing your best today.");
                sentence.setExplain("对明天最好的准备就是今天做到最好。");
            }

            model.addAttribute("sentence", sentence);

            return "main";
        }
        //跳转管理员主页
        @RequestMapping("/admin/main")
        public String toAdminPage(Model model){


            return "admin/main";
        }


}
