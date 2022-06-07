// Created: 28.10.2018
package de.freese.spring.jwt.controller;

import java.security.Principal;

import javax.annotation.Resource;

import de.freese.spring.jwt.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("users")
public class UserController
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    /**
     *
     */
    @Resource
    private UserService userService;

    /**
     * @param username String
     *
     * @return String
     */
    @DeleteMapping("delete/{username}")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Secured("ROLE_ADMIN")
    public String delete(@PathVariable final String username)
    {
        this.userService.delete(username);

        return username;
    }

    /**
     * @param username String
     * @param password String
     *
     * @return String
     */
    @GetMapping("login")
    public String login(@RequestParam final String username,
                        @RequestParam final String password)
    {
        return this.userService.login(username, password);
    }

    /**
     * @param userDetails {@link UserDetails}
     * @param user {@link AuthenticationPrincipal}
     *
     * @return String
     */
    @PostMapping("register")
    @Secured("ROLE_ADMIN")
    public String register(@RequestBody final UserDetails userDetails, @AuthenticationPrincipal final UserDetails user)
    {
        LOGGER.info("register called by '{}' in the role '{}'", user.getUsername(), user.getAuthorities());

        return this.userService.register(userDetails);
    }

    /**
     * @param username String
     * @param user {@link AuthenticationPrincipal}
     *
     * @return {@link UserDetails}
     */
    @GetMapping("search/{username}")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Secured("ROLE_ADMIN")
    public UserDetails search(@PathVariable final String username, @AuthenticationPrincipal final UserDetails user)
    {
        LOGGER.info("search called by '{}' in the role '{}'", user.getUsername(), user.getAuthorities());

        return this.userService.search(username);
    }

    /**
     * @param principal {@link Principal}
     * @param user {@link AuthenticationPrincipal}
     *
     * @return {@link Principal}
     */
    @GetMapping("me")
    // @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @Secured(
            {
                    "ROLE_ADMIN", "ROLE_USER"
            })
    public Principal whoami(final Principal principal, @AuthenticationPrincipal final UserDetails user)
    {
        LOGGER.info("whoami called by '{}'", principal.getName());
        LOGGER.info("whoami called by '{}' in the role '{}'", user.getUsername(), user.getAuthorities());

        return principal;
    }
    // public MutableUser whoami(final HttpServletRequest req)
    // {
    // return this.userService.whoami(req);
    // }
}
