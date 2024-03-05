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

    // How to do a programmatic login with Spring Security?
    // UserDetails principal = userDetailsService.loadUserByUsername(username);
    // Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
    // SecurityContext context = SecurityContextHolder.createEmptyContext();
    // context.setAuthentication(authentication);

    // How do I programmatically access the currently authenticated user in Spring Security?
    // SecurityContext context = SecurityContextHolder.getContext();
    // Authentication authentication = context.getAuthentication();
    // String username = authentication.getName();
    // Object principal = authentication.getPrincipal();
    // Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
}
