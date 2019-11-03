/**
 * Created: 22.10.2018
 */

package de.freese.spring.oauth2.resource.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@Profile(
{
        "jdbc", "memory"
})
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration
{
    /**
     * Erstellt ein neues {@link MethodSecurityConfig} Object.
     */
    public MethodSecurityConfig()
    {
        super();
    }

    /**
     * @see org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration#createExpressionHandler()
     */
    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler()
    {
        return new OAuth2MethodSecurityExpressionHandler();
    }
}
