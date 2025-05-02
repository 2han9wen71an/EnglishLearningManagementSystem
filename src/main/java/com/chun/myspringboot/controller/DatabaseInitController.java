package com.chun.myspringboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class DatabaseInitController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/initDialogueDatabase")
    public String initDialogueDatabase(Model model) {
        try {
            // 读取SQL文件内容
            ClassPathResource resource = new ClassPathResource("sql/dialogue_init.sql");
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            String sqlScript = FileCopyUtils.copyToString(reader);
            
            // 分割SQL语句
            List<String> sqlStatements = Arrays.asList(sqlScript.split(";"));
            
            // 执行每条SQL语句
            int successCount = 0;
            for (String sql : sqlStatements) {
                if (!sql.trim().isEmpty()) {
                    try {
                        jdbcTemplate.execute(sql);
                        successCount++;
                    } catch (Exception e) {
                        model.addAttribute("error", "执行SQL语句时出错: " + e.getMessage());
                        model.addAttribute("successCount", successCount);
                        model.addAttribute("totalCount", sqlStatements.size());
                        return "admin/database-init-result";
                    }
                }
            }
            
            model.addAttribute("success", true);
            model.addAttribute("message", "成功执行了 " + successCount + " 条SQL语句，情景对话数据库初始化完成！");
            
        } catch (IOException e) {
            model.addAttribute("error", "读取SQL文件时出错: " + e.getMessage());
        }
        
        return "admin/database-init-result";
    }
}
