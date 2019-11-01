/**
 * Created: 31.10.2019
 */

package de.freese.spring.oauth2.authorisation.config;

import javax.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter
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
    private TokenStore tokenStore = null;

    /**
     * Erstellt ein neues {@link ResourceServerConfig} Object.
     */
    public ResourceServerConfig()
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
                .anyRequest()
                    .hasAnyAuthority("ROLE_ADMIN")
//                  .access("#oauth2.hasScope('read')")
        ;
        // @formatter:on
    }

    /**
     * @see org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter#configure(org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer)
     */
    @Override
    public void configure(final ResourceServerSecurityConfigurer resources)
    {
        resources.resourceId(RESOURCE_ID).tokenStore(this.tokenStore);
    }
}
