package de.freese.spring.jwt.controller;

import java.security.Principal;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home(final Principal principal) {
        return "Hello, " + principal.getName();
    }

    @GetMapping("/admin")
    // @PreAuthorize("hasAuthority('ADMIN')")
    @Secured("ADMIN")
    public String secureForAdmin() {
        return "This is available for Admins!";
    }

    @GetMapping("/user")
    @PreAuthorize("hasAuthority('USER')")
    // @Secured("USER")
    public String secureForUser() {
        return "This is available for Users!";
    }
}
