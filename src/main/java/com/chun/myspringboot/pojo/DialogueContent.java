package com.chun.myspringboot.pojo;

import lombok.Data;

@Data
public class DialogueContent {
    private Integer contentId;
    private Integer scenarioId;
    private String role;
    private String content;
    private String prompt;
    private Integer orderNum;
}
