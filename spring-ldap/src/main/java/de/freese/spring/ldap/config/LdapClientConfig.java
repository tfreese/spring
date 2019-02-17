/**
 * Created: 17.02.2019
 */

package de.freese.spring.ldap.config;

import javax.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Thomas Freese
 */
@Configuration
public class LdapClientConfig
{
    /**
     *
     */
    @Resource
    private Environment env = null;

    /**
     * Erstellt ein neues {@link LdapClientConfig} Object.
     */
    public LdapClientConfig()
    {
        super();
    }

    // /**
    // * @return {@link ContextSource}
    // */
    // @Bean
    // public ContextSource ldapContextSource()
    // {
    // LdapContextSource contextSource = new LdapContextSource();
    //
    // contextSource.setUrl("ldap://localhost:" + this.env.getRequiredProperty("spring.ldap.embedded.port"));
    // contextSource.setBase(this.env.getRequiredProperty("spring.ldap.embedded.base-dn"));
    // contextSource.setUserDn("uid=admin,ou=system");
    // contextSource.setPassword("secret");
    //
    // return contextSource;
    // }

    // /**
    // * @param ldapContextSource {@link ContextSource}
    // * @return {@link LdapTemplate}
    // */
    // @Bean
    // public LdapTemplate ldapTemplate(final ContextSource ldapContextSource)
    // {
    // return new LdapTemplate(ldapContextSource);
    // }
}
