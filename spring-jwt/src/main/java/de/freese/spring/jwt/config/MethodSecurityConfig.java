/**
 * Created: 22.10.2018
 */

package de.freese.spring.jwt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration
{
    /**
     * Erstellt ein neues {@link MethodSecurityConfig} Object.
     */
    public MethodSecurityConfig()
    {
        super();

    }

    // /**
    // * @see org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration#createExpressionHandler()
    // */
    // @Override
    // protected MethodSecurityExpressionHandler createExpressionHandler()
    // {
    // return new OAuth2MethodSecurityExpressionHandler();
    // }
}
