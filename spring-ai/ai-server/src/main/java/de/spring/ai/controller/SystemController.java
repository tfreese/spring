package de.spring.ai.controller;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
public class SystemController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemController.class);

    @Resource
    private ApplicationContext applicationContext;

    @GetMapping("/shutdown")
    @ResponseStatus(HttpStatus.OK)
    public void shutdown() {
        LOGGER.info("shutdown...");

        Thread.startVirtualThread(() -> {
            final int exitCode = SpringApplication.exit(applicationContext);
            LOGGER.info("...with exit code: {}", exitCode);
            System.exit(exitCode);
        });
    }
}
