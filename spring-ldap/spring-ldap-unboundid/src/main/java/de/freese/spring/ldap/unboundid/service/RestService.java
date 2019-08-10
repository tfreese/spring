/**
 * Created: 17.02.2019
 */

package de.freese.spring.ldap.unboundid.service;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class RestService
{
    /**
     * Erstellt ein neues {@link RestService} Object.
     */
    public RestService()
    {
        super();
    }

    /**
     * @return String
     */
    @GetMapping("/")
    public String index()
    {
        return "Welcome to the home page!";
    }
}
