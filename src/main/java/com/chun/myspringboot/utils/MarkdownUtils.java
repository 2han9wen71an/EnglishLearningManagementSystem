package com.chun.myspringboot.utils;

import org.springframework.stereotype.Component;

/**
 * Markdown工具类
 * 用于将Markdown格式的文本转换为HTML格式
 */
@Component
public class MarkdownUtils {

    /**
     * 将Markdown格式的文本转换为HTML格式
     * 这是一个简单的实现，只处理最常见的Markdown语法
     * 
     * @param markdown Markdown格式的文本
     * @return HTML格式的文本
     */
    public String markdownToHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        
        // 处理标题
        markdown = markdown.replaceAll("(?m)^# (.*?)$", "<h1>$1</h1>");
        markdown = markdown.replaceAll("(?m)^## (.*?)$", "<h2>$1</h2>");
        markdown = markdown.replaceAll("(?m)^### (.*?)$", "<h3>$1</h3>");
        
        // 处理粗体和斜体
        markdown = markdown.replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>");
        markdown = markdown.replaceAll("\\*(.*?)\\*", "<em>$1</em>");
        
        // 处理列表
        markdown = markdown.replaceAll("(?m)^- (.*?)$", "<li>$1</li>");
        markdown = markdown.replaceAll("(?m)^\\d+\\. (.*?)$", "<li>$1</li>");
        
        // 处理段落和换行
        markdown = markdown.replaceAll("(?m)^(?!<[hlu])(.*?)$", "<p>$1</p>");
        markdown = markdown.replaceAll("<p>\\s*</p>", "");
        
        // 处理链接
        markdown = markdown.replaceAll("\\[(.*?)\\]\\((.*?)\\)", "<a href=\"$2\">$1</a>");
        
        // 处理代码块
        markdown = markdown.replaceAll("`(.*?)`", "<code>$1</code>");
        
        return markdown;
    }
}
