package com.chun.myspringboot.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Controller
public class EssayPronunciationDatabaseInitController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 初始化情景对话数据库
    @RequestMapping("/admin/initDialogueDatabase")
    public String initDialogueDatabase(Model model) {
        try {
            // 读取SQL文件
            String sql = readSqlFile("sql/dialogue_tables.sql");

            // 执行SQL语句
            String[] sqlStatements = sql.split(";");
            int totalCount = 0;
            int successCount = 0;

            for (String statement : sqlStatements) {
                if (!statement.trim().isEmpty()) {
                    totalCount++;
                    try {
                        jdbcTemplate.execute(statement);
                        successCount++;
                    } catch (Exception e) {
                        // 继续执行下一条语句
                    }
                }
            }

            if (successCount == totalCount) {
                model.addAttribute("success", true);
                model.addAttribute("message", "情景对话数据库初始化成功！共执行 " + successCount + " 条SQL语句。");
            } else {
                model.addAttribute("error", "部分SQL语句执行失败，可能是表已存在。");
                model.addAttribute("successCount", successCount);
                model.addAttribute("totalCount", totalCount);
            }
        } catch (Exception e) {
            model.addAttribute("error", "初始化失败: " + e.getMessage());
        }

        return "admin/database-init-result";
    }

    // 初始化作文和发音评测数据库
    @RequestMapping("/admin/initEssayPronunciationDatabase")
    public String initEssayPronunciationDatabase(Model model) {
        try {
            // 读取SQL文件
            String sql = readSqlFile("sql/essay_pronunciation_tables.sql");

            // 执行SQL语句
            String[] sqlStatements = sql.split(";");
            int totalCount = 0;
            int successCount = 0;

            for (String statement : sqlStatements) {
                if (!statement.trim().isEmpty()) {
                    totalCount++;
                    try {
                        jdbcTemplate.execute(statement);
                        successCount++;
                    } catch (Exception e) {
                        // 继续执行下一条语句
                    }
                }
            }

            if (successCount == totalCount) {
                model.addAttribute("success", true);
                model.addAttribute("message", "作文和发音评测数据库初始化成功！共执行 " + successCount + " 条SQL语句。");
            } else {
                model.addAttribute("error", "部分SQL语句执行失败，可能是表已存在。");
                model.addAttribute("successCount", successCount);
                model.addAttribute("totalCount", totalCount);
            }
        } catch (Exception e) {
            model.addAttribute("error", "初始化失败: " + e.getMessage());
        }

        return "admin/database-init-result";
    }

    // 读取SQL文件内容
    private String readSqlFile(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}
