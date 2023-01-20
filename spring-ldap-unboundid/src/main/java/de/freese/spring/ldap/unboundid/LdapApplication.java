// Created: 09.02.2019
package de.freese.spring.ldap.unboundid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <a href="https://github.com/spring-guides/gs-authenticating-ldap">gs-authenticating-ldap</a><br>
 * <a href="https://github.com/eugenp/tutorials/tree/master/spring-ldap">github</a><br>
 * <a href="https://www.baeldung.com/spring-ldap">spring-ldap</a><br>
 *
 * @author Thomas Freese
 */
@SpringBootApplication
public class LdapApplication
{
    public static void main(final String[] args)
    {
        SpringApplication.run(LdapApplication.class, args);
    }
}
