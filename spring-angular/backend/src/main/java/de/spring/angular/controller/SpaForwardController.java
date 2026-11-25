package de.spring.angular.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {
    @GetMapping({"/", "/tasks"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}

