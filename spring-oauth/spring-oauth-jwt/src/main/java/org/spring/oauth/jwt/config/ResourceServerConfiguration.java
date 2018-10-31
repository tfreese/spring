package org.spring.oauth.jwt.config;

import javax.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter
{
    /**
     * Prüfung des Client-Requests auf OAuth Authorization.
     */
    private static final RequestMatcher oAuthRequestedMatcher = request -> {
        String auth = request.getHeader("Authorization");

        boolean haveOauth2Token = (auth != null) && auth.startsWith("Bearer");
        boolean haveAccessToken = request.getParameter("access_token") != null;

        return haveOauth2Token || haveAccessToken;
    };

    /**
     *
     */
    private static final String RESOURCE_ID = "my-oauth-app";

    /**
    *
    */
    @Resource
    private ResourceServerTokenServices tokenServices = null;

    // /**
    // *
    // */
    // @Resource
    // private TokenStore tokenStore = null;

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
            .requestMatcher(oAuthRequestedMatcher)
            .authorizeRequests()
                .antMatchers("/auth/oauth/token").permitAll()
                .anyRequest().hasAuthority("ROLE_USER")
                .anyRequest().access("#oauth2.hasScope('read')")
        ;
        // @formatter:on
    }

    /**
     * @see org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter#configure(org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer)
     */
    @Override
    public void configure(final ResourceServerSecurityConfigurer resources)
    {
        // @formatter:off
        resources
            .resourceId(RESOURCE_ID)
            //.tokenStore(this.tokenStore)
            .tokenServices(this.tokenServices)
        ;
        // @formatter:on
    }
}
