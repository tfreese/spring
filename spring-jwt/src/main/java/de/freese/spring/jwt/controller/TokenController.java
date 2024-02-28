package de.freese.spring.jwt.controller;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import de.freese.spring.jwt.service.TokenService;

@RestController
public class TokenController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenController.class);

    private final TokenService tokenService;

    public TokenController(final TokenService tokenService) {
        super();

        this.tokenService = Objects.requireNonNull(tokenService, "tokenService required");
    }

    @PostMapping("/token")
    public String token(final Authentication authentication, @AuthenticationPrincipal final UserDetails user) {
        LOGGER.info("Token requested for Authentication: '{}'", authentication);
        LOGGER.info("Token requested for User: {}", user);

        final String token = tokenService.generateToken(authentication);
        LOGGER.info("Token granted: {}", token);

        return token;
    }
}
