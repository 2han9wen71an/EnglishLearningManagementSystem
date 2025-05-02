package com.chun.myspringboot.pojo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DialogueHistory {
    private Integer scenarioId;
    private String scenarioName;
    private String scenarioDescription;
    private List<String> messages = new ArrayList<>();
    
    public void addMessage(String message) {
        messages.add(message);
    }
    
    public List<String> getMessages() {
        return messages;
    }
    
    public void clearMessages() {
        messages.clear();
    }
}
