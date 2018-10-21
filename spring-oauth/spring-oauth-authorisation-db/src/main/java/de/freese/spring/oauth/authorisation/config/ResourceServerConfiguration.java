package de.freese.spring.oauth.authorisation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter
{
    /**
     *
     */
    private static final String RESOURCE_ID = "my-oauth-app";

    /**
     * Erstellt ein neues {@link ResourceServerConfiguration} Object.
     */
    public ResourceServerConfiguration()
    {
        super();
    }

    /**
     * @see org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
     */
    @Override
    public void configure(final HttpSecurity http) throws Exception
    {
        // @formatter:off
        http
            .authorizeRequests()
                .antMatchers("/auth/oauth/token").permitAll()
                //.antMatchers(HttpMethod.POST, "/auth/rest").access("#oauth2.hasScope('write')")
                .anyRequest().access("#oauth2.hasScope('read')");
        // @formatter:on
    }

    /**
     * @see org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter#configure(org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer)
     */
    @Override
    public void configure(final ResourceServerSecurityConfigurer resources)
    {
        resources.resourceId(RESOURCE_ID);
    }
}
