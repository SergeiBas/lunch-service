package com.lunch.common.config;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/manager/test")
    public String manager() { return "Order manager area"; }

    @GetMapping("/collector/test")
    public String collector() { return "Collector manager area"; }

    @GetMapping("/courier/test")
    public String courier() { return "Courier area"; }

    @GetMapping("/test/error")
    public String testError() {
        throw new RuntimeException("Тестова помилка");
    }
}