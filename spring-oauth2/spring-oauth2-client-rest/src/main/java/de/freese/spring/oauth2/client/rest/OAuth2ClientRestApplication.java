/**
 * Created: 31.10.2019
 */

package de.freese.spring.oauth2.client.rest;

import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.web.client.RestTemplate;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
// @EnableOAuth2Client
public class OAuth2ClientRestApplication extends SpringBootServletInitializer
{
    /**
     * @param args String[]
     * @throws Exception Falls was schief geht.
     */
    public static void main(final String[] args) throws Exception
    {
        SpringApplication.run(OAuth2ClientRestApplication.class, args);
    }

    /**
     * Erstellt ein neues {@link OAuth2ClientRestApplication} Object.
     */
    public OAuth2ClientRestApplication()
    {
        super();
    }

    // /**
    // * Manuelle Definition
    // *
    // * @return {@link OAuth2ProtectedResourceDetails}
    // */
    // @Bean
    // public ResourceOwnerPasswordResourceDetails myAppOauthDetails()
    // {
    // // AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
    // // ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();
    //
    // ResourceOwnerPasswordResourceDetails resourceDetails = new ResourceOwnerPasswordResourceDetails();
    // resourceDetails.setClientId("my-app");
    // resourceDetails.setClientSecret("app-secret");
    // resourceDetails.setAccessTokenUri("http://localhost:9999/authsrv/oauth/token");
    // resourceDetails.setUsername("user");
    // resourceDetails.setPassword("pw");
    //
    // return resourceDetails;
    // }

    /**
     * @param resource {@link OAuth2ProtectedResourceDetails}
     * @param context {@link OAuth2ClientContext}
     * @return {@link RestTemplate}
     */
    @Bean
    public RestTemplate oAuth2RestTemplate(final OAuth2ProtectedResourceDetails resource, final OAuth2ClientContext context)
    {
        OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(resource, context);

        oAuth2RestTemplate.setInterceptors(Arrays.asList(new BasicAuthenticationInterceptor("user", "pw")));

        // Validierung
        // oAuth2RestTemplate.getAccessToken();

        return oAuth2RestTemplate;
    }
}
