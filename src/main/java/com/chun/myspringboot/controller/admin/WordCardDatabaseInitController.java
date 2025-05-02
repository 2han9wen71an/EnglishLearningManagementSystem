package com.chun.myspringboot.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Controller
public class WordCardDatabaseInitController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 初始化单词卡片数据库
     */
    @RequestMapping("/admin/initWordCardDatabase")
    public String initWordCardDatabase(Model model) {
        try {
            // 读取SQL文件
            InputStream inputStream = getClass().getResourceAsStream("/sql/word_card_table.sql");
            String sql = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));

            // 执行SQL语句
            jdbcTemplate.execute(sql);

            model.addAttribute("success", true);
            model.addAttribute("message", "单词卡片数据库初始化成功！");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "单词卡片数据库初始化失败：" + e.getMessage());
        }

        return "admin/database-init-result";
    }
}
