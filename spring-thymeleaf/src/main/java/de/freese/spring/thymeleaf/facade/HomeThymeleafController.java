// Created: 05.09.2018
package de.freese.spring.thymeleaf.facade;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.freese.spring.thymeleaf.ThymeleafController;

/**
 * @author Thomas Freese
 */
@ThymeleafController
public class HomeThymeleafController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeThymeleafController.class);

    @Value("${app.message.welcome}")
    private final String message = "Hello World";

    @Resource
    @Qualifier("authenticationManagerWeb")
    private AuthenticationManager authenticationManager;

    @Resource
    private UserDetailsService userDetailsService;

    @GetMapping("/createError")
    public void createError() {
        // throw new IllegalArgumentException("Test Exception");
        throw new IllegalStateException("Test Exception");
    }

    @GetMapping(value = {"/", "/index"})
    public String index(final Model model, final Principal principal)
    // public String index(final Map<String, Object> model)
    {
        model.addAttribute("message", this.message);

        // return principal != null ? "/login" : "/index";
        return "/index";
    }

    @GetMapping("/login")
    public String login() {
        return "/authentication/login";
    }

    protected void loginWithHttpServletRequest(final HttpServletRequest req, @RequestParam final String user, @RequestParam final String pass) {
        try {
            req.login(user, pass);
        }
        catch (ServletException sex) {
            LOGGER.error("Error while login ", sex);

            throw new RuntimeException(sex);
        }
    }

    /**
     * curl -X POST http://localhost:8080/rest/auth?token=admin
     */
    @PostMapping("/loginWithToken")
    protected String loginWithToken(final HttpServletRequest req, @RequestParam final String token) {
        final UserDetails userDetails = this.userDetailsService.loadUserByUsername(token);
        final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        final SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);

        final HttpSession session = req.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

        LOGGER.info("User logged in. username={}, token={}", userDetails.getUsername(), token);

        // return "forward:/user";
        return "redirect:/user";
    }

    @PostMapping("/loginWithUserAndPassword")
    protected void loginWithUserAndPassword(final HttpServletRequest req, @RequestParam final String user, @RequestParam final String pass) {
        final UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(user, pass);
        final Authentication auth = this.authenticationManager.authenticate(authReq);

        final SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);

        final HttpSession session = req.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
    }

    @PostMapping("/loginWithoutPassword")
    protected void loginWithoutPassword(final HttpServletRequest req, @RequestParam final User user) {
        // final List<Privilege> privileges = user.getRoles().stream()
        // .map(role -> role.getPrivileges())
        // .flatMap(list -> list.stream())
        // .distinct().collect(Collectors.toList());

        final List<GrantedAuthority> authorities = new ArrayList<>(user.getAuthorities());

        final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, authorities);

        final SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);

        final HttpSession session = req.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
    }

    @GetMapping("/logout")
    protected String logout(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            authentication.setAuthenticated(false);

            new SecurityContextLogoutHandler().logout(request, response, authentication);

            SecurityContextHolder.clearContext();
            request.logout();
            request.getSession().invalidate();
        }

        return "redirect:/login?logout";
    }
}
