/*** Created:31.10.2019 */

package de.freese.spring.oauth2.client.ui.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Der ResourceServer stellt Ressourcen (REST-Schnittstellen) zur Verfügung, die durch den Autorisierungsserver abgesichert sein sollen.
 *
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
    @Value("${spring.security.oauth2.client.registration.custom-client.client-id}")
    private String clientId = null;

    /**
    *
    */
    @Value("${spring.security.oauth2.client.registration.custom-client.client-secret}")
    private String clientSecret = null;

    // /**
    // *
    // */
    // @Resource
    // private TokenStore tokenStore = null;

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
                .antMatchers("/", "/unsecured").permitAll()
                .anyRequest().hasAnyAuthority("ROLE_USER")
                //.anyRequest().access("#oauth2.hasScope('read')")
        ;
        // @formatter:on
    }

    /**
     * @see org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter#configure(org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer)
     */
    @Override
    public void configure(final ResourceServerSecurityConfigurer resources)
    {
        resources.resourceId(this.clientId);
        // .tokenStore(this.tokenStore);
    }

    /**
     * @return {@link RemoteTokenServices}
     */
    @Primary
    @Bean
    public RemoteTokenServices tokenServices()
    {
        RemoteTokenServices tokenService = new RemoteTokenServices();
        tokenService.setCheckTokenEndpointUrl("http://localhost:9999/authsrv/oauth/check_token"); // Nur für JWT-Relevant
        tokenService.setClientId(this.clientId);
        tokenService.setClientSecret(this.clientSecret);

        return tokenService;
    }
}
