// Created: 05.04.2025
package de.freese.spring.ott;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Thomas Freese
 */
@Controller
public final class PageController {
    @GetMapping
    public String index(final Principal principal, final Model model) {
        model.addAttribute("user", principal.getName());

        return "index";
    }

    @GetMapping("/ott/sent")
    public String ottSent() {
        return "ott-sent";
    }
}
