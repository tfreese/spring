/**
 * Created: 20.01.2018
 */

package de.freese.spring.security.rest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping(path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class SecuredRestController {
    /**
     * curl http://localhost:10000/spring-security/rest/admin -u admin:admin1
     *
     * @return String
     */
    @GetMapping("/admin")
    public String getAdmin() {
        return "Secret Admin Message";
    }

    /**
     * curl http://localhost:10000/spring-security/rest/
     *
     * @return String
     */
    @GetMapping("/")
    public String getPublic() {
        return "Public Message";
    }

    /**
     * curl http://localhost:10000/spring-security/rest/user -u user:user1
     *
     * @return String
     */
    @GetMapping("/user")
    public String getUser() {
        return "Secret User Message";
    }
}
