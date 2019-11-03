/*** Created:31.10.2019 */

package de.freese.spring.oauth2.resource.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Der ResourceServer stellt Ressourcen (REST-Schnittstellen) zur Verfügung, die durch den Autorisierungsserver abgesichert sein sollen.
 *
 * @author Thomas Freese
 */
@Configuration
@EnableResourceServer
@Order(1)
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
    @Value("${security.oauth2.client.clientId}")
    private String clientId = null;

    /**
     *
     */
    @Value("${security.oauth2.client.clientSecret}")
    private String clientSecret = null;

    /**
     *
     */
    @Value("${security.oauth2.resource.token-info-uri}")
    private String tokenEndpoint = null;

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
                .anyRequest().hasAnyAuthority("ROLE_USER")
                //.anyRequest().access("#oauth2.hasScope('read')")
        ;
        // @formatter:on
    }

    /**
     * @return {@link RemoteTokenServices}
     */
    @Primary
    @Bean
    public RemoteTokenServices tokenServices()
    {
        RemoteTokenServices tokenService = new RemoteTokenServices();
        tokenService.setCheckTokenEndpointUrl(this.tokenEndpoint);
        tokenService.setClientId(this.clientId);
        tokenService.setClientSecret(this.clientSecret);

        return tokenService;
    }
}
