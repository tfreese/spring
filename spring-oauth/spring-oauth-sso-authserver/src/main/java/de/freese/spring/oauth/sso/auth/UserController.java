package de.freese.spring.oauth.sso.auth;

import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("user")
public class UserController
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    /**
     * Erstellt ein neues {@link UserController} Object.
     */
    public UserController()
    {
        super();
    }

    /**
     * @return String
     */
    @GetMapping("hello")
    // @Secured("ROLE_USER")
    public String hello()
    {
        return "Hello World";
    }

    /**
     * @param principal {@link Principal}
     * @return {@link Principal}
     */
    @RequestMapping("me")
    public Principal user(final Principal principal)
    {
        LOGGER.info(principal != null ? principal.toString() : "null");

        return principal;
    }
}
