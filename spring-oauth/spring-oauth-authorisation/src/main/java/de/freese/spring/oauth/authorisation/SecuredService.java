/**
 * Created: 25.09.2018
 */

package de.freese.spring.oauth.authorisation;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("rest")
public class SecuredService
{
    /**
     *
     */
    private String message = "Hello World";

    /**
     * Erstellt ein neues {@link SecuredService} Object.
     */
    public SecuredService()
    {
        super();
    }

    /**
     * @return String
     */
    @GetMapping("message")
    @PreAuthorize("#oauth2.hasScope('read')")
    @Secured("ROLE_USER")
    public String getMessage()
    {
        return this.message;
    }

    /**
     * @param message String
     */
    @PostMapping("message/{message}")
    @PreAuthorize("#oauth2.hasScope('write')")
    @Secured("ROLE_ADMIN")
    public void setMessage(@PathVariable("message") final String message)
    {
        this.message = message;
    }
}
