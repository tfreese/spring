package de.spring.react.controller;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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
    public ResponseEntity<String> getTime() {
        final OffsetDateTime nowUtcMs = OffsetDateTime.now(ZoneId.systemDefault())
                .truncatedTo(ChronoUnit.MILLIS);

        // Nur dieses Format kann react parsen.
        // z.B. 2026-10-19T17:19:09.403Z > @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        final String iso = nowUtcMs.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return ResponseEntity.ok(iso);
    }
}
