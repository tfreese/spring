package org.spring.oauth.resource;

import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
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

    // /**
    // *
    // */
    // @Resource
    // private OAuth2ProtectedResourceDetails oAuth2ProtectedResourceDetails = null;

    /**
     * @return {@link OAuth2ProtectedResourceDetails}
     */
    @Bean
    public OAuth2ProtectedResourceDetails oAuth2ProtectedResourceDetails()
    {
        // AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
        BaseOAuth2ProtectedResourceDetails details = new ClientCredentialsResourceDetails();
        details.setId("local");
        details.setClientId("SampleClientId");
        details.setClientSecret("secret");
        details.setAccessTokenUri("http://localhost:8081/auth/oauth/token");
        // details.setUserAuthorizationUri("http://localhost:8081/auth/oauth/authorize");
        // details.setTokenName("oauth_token");
        // details.setScope(Arrays.asList("identity"));
        // details.setPreEstablishedRedirectUri("http://localhost/login");
        // details.setUseCurrentUri(false);
        return details;
    }

    /**
     * @param clientContext {@link OAuth2ClientContext}
     * @return RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(final OAuth2ClientContext clientContext)
    {
        OAuth2RestTemplate template = new OAuth2RestTemplate(oAuth2ProtectedResourceDetails(), clientContext);
        // AccessTokenProvider accessTokenProvider = new AccessTokenProviderChain(
        // Arrays.asList(new ImplicitAccessTokenProvider(), new ResourceOwnerPasswordAccessTokenProvider(), new ClientCredentialsAccessTokenProvider()));
        // template.setAccessTokenProvider(accessTokenProvider);

        template.setInterceptors(Arrays.asList(new BasicAuthorizationInterceptor("john", "123")));

        return template;
    }
}