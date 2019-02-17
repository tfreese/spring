/**
 * Created: 09.02.2019
 */

package de.freese.spring.ldap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * https://github.com/spring-guides/gs-authenticating-ldap<br>
 * https://github.com/eugenp/tutorials/tree/master/spring-ldap<br>
 * https://www.baeldung.com/spring-ldap<br>
 *
 * @author Thomas Freese
 */
@SpringBootApplication
public class LdapApplication
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        SpringApplication.run(LdapApplication.class, args);
    }

    /**
     * Erstellt ein neues {@link LdapApplication} Object.
     */
    public LdapApplication()
    {
        super();
    }
}
