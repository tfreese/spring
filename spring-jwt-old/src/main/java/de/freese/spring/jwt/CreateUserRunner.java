// Created: 12.09.2018
package de.freese.spring.jwt;

import java.util.Arrays;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import de.freese.spring.jwt.service.UserService;

/**
 * @author Thomas Freese
 */
//@Component
//@Order(10)
public class CreateUserRunner implements CommandLineRunner {
    public static final Logger LOGGER = LoggerFactory.getLogger(CreateUserRunner.class);

    @Resource
    private UserService userService;

    @Override
    public void run(final String... args) throws Exception {
        LOGGER.info("Create Users & Roles");

        final User admin = new User("admin", "{noop}pass", Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER")));
        String token = this.userService.register(admin);
        LOGGER.info("Admin Token: {}", token);

        final User user = new User("user", "{noop}pass", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
        token = this.userService.register(user);
        LOGGER.info("User Token: {}", token);
    }
}
