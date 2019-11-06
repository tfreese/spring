/**
 * Created: 25.09.2018
 */

package de.freese.spring.oauth2.client.web;

import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("secured")
public class SecuredController
{
    /**
     * Erstellt ein neues {@link SecuredController} Object.
     */
    public SecuredController()
    {
        super();
    }

    /**
     * @param auth {@link Authentication}
     * @return String
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String securedResource(final Authentication auth)
    {
        return "This is a SECURED resource. Authentication: " + auth.getName() + "; Authorities: " + auth.getAuthorities();
    }

    /**
     * @param principal {@link Principal}
     * @return {@link Principal}
     */
    @GetMapping("user/me")
    @PreAuthorize("#oauth2.hasScope('write') and #oauth2.hasScope('read')")
    public Principal user(final Principal principal)
    {
        return principal;
    }
}
