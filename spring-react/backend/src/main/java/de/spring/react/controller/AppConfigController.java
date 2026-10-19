package de.spring.react.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 * @since 19.10.26
 */
@RestController
public class AppConfigController {
    @Value("${app.api.base}")
    private String apiBase;

    @Value("${app.public.base}")
    private String publicBase;

    @GetMapping("/app-config")
    public Map<String, String> appConfig() {
        return Map.of(
                "apiBase", apiBase,
                "publicBase", publicBase
        );
    }
}