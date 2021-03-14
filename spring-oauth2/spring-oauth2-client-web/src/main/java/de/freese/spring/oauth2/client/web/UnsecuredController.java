/**
 * Created: 25.09.2018
 */
package de.freese.spring.oauth2.client.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("unsecured")
public class UnsecuredController
{
    /**
     * @return String
     */
    @GetMapping
    public String unsecuredResource()
    {
        return "This is an unsecured resource";
    }
}
