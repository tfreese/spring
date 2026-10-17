package de.spring.react.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 * @since 17.10.26
 */
@RestController
@RequestMapping("/public")
public final class PublicController {
    @GetMapping("/time")
    public ResponseEntity<LocalDateTime> getTime() {
        return ResponseEntity.ok(LocalDateTime.now(ZoneId.systemDefault()));
    }
}
