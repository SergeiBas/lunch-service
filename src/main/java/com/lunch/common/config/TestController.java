package com.lunch.common.config;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

//    @GetMapping("/profile")
//    public String profile(Authentication auth) { return "Profile of " + auth.getName(); }

    @GetMapping("/manager/test")
    public String manager() { return "Order manager area"; }

    @GetMapping("/collector/test")
    public String collector() { return "Collector manager area"; }

    @GetMapping("/courier/test")
    public String courier() { return "Courier area"; }
}