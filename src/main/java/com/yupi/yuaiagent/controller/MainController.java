package com.yupi.yuaiagent.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class MainController {
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}
