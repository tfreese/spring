/**
 * Created: 21.01.2018
 */
package de.freese.spring.security.rest;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping(path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class LoginController
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    /**
     *
     */
    @Resource
    private AuthenticationManager authenticationManager;

    /**
     *
     */
    @Resource
    private UserDetailsService userDetailsService;

    /**
     * @param authToken String
     *
     * @return String
     */
    public String findUsernameByToken(final String authToken)
    {
        return authToken;
    }

    /**
     * @param req {@link HttpServletRequest}
     * @param user String
     * @param pass String
     */
    public void loginWithHttpServletRequest(final HttpServletRequest req, @RequestParam final String user, @RequestParam final String pass)
    {
        try
        {
            req.login(user, pass);
        }
        catch (ServletException sex)
        {
            LOGGER.error("Error while login ", sex);

            throw new RuntimeException(sex);
        }
    }

    /**
     * @param req {@link HttpServletRequest}
     * @param user {@link User}
     */
    @PostMapping("/loginWithoutPassword")
    public void loginWithoutPassword(final HttpServletRequest req, @RequestParam final User user)
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
     * curl -X POST http://localhost:10000/spring-security/rest/auth?token=admin
     *
     * @param req {@link HttpServletRequest}
     * @param token String
     *
     * @return String
     */
    @PostMapping("/loginWithToken")
    public String loginWithToken(final HttpServletRequest req, @RequestParam final String token)
    {
        // This whole stuff should be inside of a service method...
        String usernameByToken = findUsernameByToken(token);

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(usernameByToken);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);

        HttpSession session = req.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

        LOGGER.info("User logged in. username={}, token={}", userDetails.getUsername(), token);

        // return "forward:/user";
        return "redirect:/user";
    }

    /**
     * @param req {@link HttpServletRequest}
     * @param user String
     * @param pass String
     */
    @PostMapping("/loginWithUserAndPassword")
    public void loginWithUserAndPassword(final HttpServletRequest req, @RequestParam final String user, @RequestParam final String pass)
    {
        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(user, pass);
        Authentication auth = this.authenticationManager.authenticate(authReq);

        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);

        HttpSession session = req.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
    }

    /**
     * @param request {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     *
     * @return String
     */
    @GetMapping("/logout")
    public String logout(final HttpServletRequest request, final HttpServletResponse response)
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null)
        {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return "redirect:/login?logout";
    }
}
