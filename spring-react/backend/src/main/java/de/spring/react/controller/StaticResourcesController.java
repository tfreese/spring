package de.spring.react.controller;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 * @since 19.10.26
 */
@RestController
public final class StaticResourcesController {
    /**
     * Leitet die Standard-Anfrage der Browser nach {@code /favicon.ico} auf das SVG-Icon des Frontends um.
     */
    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, URI.create("/favicon.svg").toString())
                .build();
    }
}
