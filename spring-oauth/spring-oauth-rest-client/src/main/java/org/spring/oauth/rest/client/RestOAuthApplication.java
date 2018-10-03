package org.spring.oauth.rest.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.client.token.grant.redirect.AbstractRedirectResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.web.client.RestTemplate;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableOAuth2Client
public class RestOAuthApplication
{
    /**
     * @param args String[]
     * @throws Exception Falls was schief geht.
     */
    public static void main(final String[] args) throws Exception
    {
        SpringApplication.run(RestOAuthApplication.class, args);
    }

    /**
     * @return {@link OAuth2ProtectedResourceDetails}
     */
    @Bean("myResourceDetails")
    public OAuth2ProtectedResourceDetails oAuth2ProtectedResourceDetails()
    {
        // AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
        // BaseOAuth2ProtectedResourceDetails details = new ClientCredentialsResourceDetails();
        BaseOAuth2ProtectedResourceDetails details = new ResourceOwnerPasswordResourceDetails();

        details.setId("local");
        details.setClientId("my-client-id");
        details.setClientSecret("{noop}secret");
        details.setAccessTokenUri("http://localhost:8081/auth/oauth/token");
        // details.setAuthenticationScheme(AuthenticationScheme.header);
        // details.setClientAuthenticationScheme(AuthenticationScheme.query);

        if (details instanceof AbstractRedirectResourceDetails)
        {
            ((AbstractRedirectResourceDetails) details).setUserAuthorizationUri("http://localhost:8081/auth/oauth/authorize");
            ((AbstractRedirectResourceDetails) details).setPreEstablishedRedirectUri("http://localhost:8082/login");
        }
        else if (details instanceof ResourceOwnerPasswordResourceDetails)
        {
            ((ResourceOwnerPasswordResourceDetails) details).setUsername("user");
            ((ResourceOwnerPasswordResourceDetails) details).setPassword("pw");
        }

        // details.setTokenName("oauth_token");
        // details.setScope(Arrays.asList("identity"));
        // details.setPreEstablishedRedirectUri("http://localhost/login");
        // details.setUseCurrentUri(false);
        return details;
    }

    /**
     * @Qualifier("myResourceDetails")
     *
     * @param oAuth2ProtectedResourceDetails {@link OAuth2ProtectedResourceDetails}
     * @param clientContext {@link OAuth2ClientContext}
     * @return RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(final OAuth2ProtectedResourceDetails oAuth2ProtectedResourceDetails, final OAuth2ClientContext clientContext)
    {
        OAuth2RestTemplate template = new OAuth2RestTemplate(oAuth2ProtectedResourceDetails, clientContext);
        // AccessTokenProvider accessTokenProvider = new AccessTokenProviderChain(
        // Arrays.asList(new ImplicitAccessTokenProvider(), new ResourceOwnerPasswordAccessTokenProvider(), new ClientCredentialsAccessTokenProvider()));
        // template.setAccessTokenProvider(accessTokenProvider);

        // template.setInterceptors(Arrays.asList(new BasicAuthorizationInterceptor("user", "pw")));

        return template;
    }
}