// Created: 22.10.2018
package de.freese.spring.jwt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = false)
public class MethodSecurityConfig //extends GlobalMethodSecurityConfiguration
{
    // @Override
    // protected MethodSecurityExpressionHandler createExpressionHandler()
    // {
    // return new OAuth2MethodSecurityExpressionHandler();
    // }
}
