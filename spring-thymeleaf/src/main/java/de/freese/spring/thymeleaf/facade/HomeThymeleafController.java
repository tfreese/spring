// Created: 05.09.2018
package de.freese.spring.thymeleaf.facade;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.freese.spring.thymeleaf.ThymeleafApplication;
import de.freese.spring.thymeleaf.ThymeleafController;
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

/**
 * @author Thomas Freese
 */
@ThymeleafController
public class HomeThymeleafController
{
    /**
     *
     */
    @Resource
    @Qualifier("authenticationManagerWeb")
    private AuthenticationManager authenticationManager;
    /**
     *
     */
    @Value("${app.message.welcome}")
    private String message = "Hello World";
    /**
     *
     */
    @Resource
    private UserDetailsService userDetailsService;

    /**
     *
     */
    @GetMapping("/createError")
    public void createError()
    {
        // throw new IllegalArgumentException("Test Exception");
        throw new IllegalStateException("Test Exception");
    }

    /**
     * @param model {@link Model}
     * @param principal {@link Principal}
     *
     * @return String
     */
    @GetMapping(value =
            {
                    "/", "/index"
            })
    public String index(final Model model, final Principal principal)
    // public String index(final Map<String, Object> model)
    {
        model.addAttribute("message", this.message);

        // return principal != null ? "/login" : "/index";
        return "/index";
    }

    /**
     * @return String
     */
    @GetMapping("/login")
    public String login()
    {
        return "/authentication/login";
    }

    /**
     * !!! DEMO !!!
     *
     * @param req {@link HttpServletRequest}
     * @param user String
     * @param pass String
     */
    protected void loginWithHttpServletRequest(final HttpServletRequest req, @RequestParam final String user, @RequestParam final String pass)
    {
        try
        {
            req.login(user, pass);
        }
        catch (ServletException sex)
        {
            ThymeleafApplication.LOGGER.error("Error while login ", sex);

            throw new RuntimeException(sex);
        }
    }

    /**
     * !!! DEMO !!!
     *
     * @param req {@link HttpServletRequest}
     * @param user {@link User}
     */
    @PostMapping("/loginWithoutPassword")
    protected void loginWithoutPassword(final HttpServletRequest req, @RequestParam final User user)
    {
        // List<Privilege> privileges = user.getRoles().stream()
        // .map(role -> role.getPrivileges())
        // .flatMap(list -> list.stream())
        // .distinct().collect(Collectors.toList());

        List<GrantedAuthority> authorities = new ArrayList<>(user.getAuthorities());

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, authorities);

        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);

        HttpSession session = req.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
    }

    /**
     * !!! DEMO !!! curl -X POST http://localhost:8080/rest/auth?token=admin
     *
     * @param req {@link HttpServletRequest}
     * @param token String
     *
     * @return String
     */
    @PostMapping("/loginWithToken")
    protected String loginWithToken(final HttpServletRequest req, @RequestParam final String token)
    {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(token);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);

        HttpSession session = req.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

        ThymeleafApplication.LOGGER.info("User logged in. username={}, token={}", userDetails.getUsername(), token);

        // return "forward:/user";
        return "redirect:/user";
    }

    /**
     * !!! DEMO !!!
     *
     * @param req {@link HttpServletRequest}
     * @param user String
     * @param pass String
     */
    @PostMapping("/loginWithUserAndPassword")
    protected void loginWithUserAndPassword(final HttpServletRequest req, @RequestParam final String user, @RequestParam final String pass)
    {
        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(user, pass);
        Authentication auth = this.authenticationManager.authenticate(authReq);

        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);

        HttpSession session = req.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
    }

    /**
     * !!! DEMO !!!
     *
     * @param request {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     *
     * @return String
     *
     * @throws ServletException Falls was schiefgeht.
     */
    @GetMapping("/logout")
    protected String logout(final HttpServletRequest request, final HttpServletResponse response) throws ServletException
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null)
        {
            authentication.setAuthenticated(false);

            new SecurityContextLogoutHandler().logout(request, response, authentication);

            SecurityContextHolder.clearContext();
            request.logout();
            request.getSession().invalidate();
        }

        return "redirect:/login?logout";
    }
}
