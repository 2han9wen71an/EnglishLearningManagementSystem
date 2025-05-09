package com.chun.myspringboot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    /**
     * 测试API接口
     * @return 测试数据
     */
    @GetMapping
    public Map<String, Object> test() {
        System.out.println("进入TEST API！！");
        Map<String, Object> result = new HashMap<>();
        result.put("message", "测试成功");
        result.put("status", "ok");
        return result;
    }
}
