/**
 * Created: 25.09.2018
 */

package de.freese.spring.oauth.authorisation;

import java.security.Principal;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("rest")
public class HelloResource
{
    /**
     * Erstellt ein neues {@link HelloResource} Object.
     */
    public HelloResource()
    {
        super();
    }

    /**
     * @return String
     */
    @GetMapping("hello")
    @Secured("ROLE_USER")
    public String hello()
    {
        return "Hello World";
    }

    /**
     * @param principal {@link Principal}
     * @return {@link Principal}
     */
    @GetMapping("me")
    public Principal user(final Principal principal)
    {
        return principal;
    }
}
