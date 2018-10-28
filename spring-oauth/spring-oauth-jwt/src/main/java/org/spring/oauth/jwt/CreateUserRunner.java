/**
 * Created: 12.09.2018
 */

package org.spring.oauth.jwt;

import java.util.Arrays;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring.oauth.jwt.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
@Order(10)
public class CreateUserRunner implements CommandLineRunner
{
    /**
    *
    */
    public static final Logger LOGGER = LoggerFactory.getLogger(CreateUserRunner.class);

    /**
     *
     */
    @Resource
    private UserService userService = null;

    /**
     * Erstellt ein neues {@link CreateUserRunner} Object.
     */
    public CreateUserRunner()
    {
        super();
    }

    /**
     * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
     */
    @Override
    public void run(final String...args) throws Exception
    {
        LOGGER.info("");

        User admin = new User("admin", "pw", Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER")));
        String token = this.userService.signup(admin);
        System.out.println("Admin Token: " + token);

        User user = new User("user", "pw", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
        token = this.userService.signup(user);
        System.out.println("User Token: " + token);
    }
}
