package com.chun.myspringboot.pojo;

import lombok.Data;

@Data
public class DialogueScenario {
    private Integer scenarioId;
    private String scenarioName;
    private String scenarioDescription;
    private String scenarioImage;
    private Integer difficultyLevel;
}
