/**
 * Created: 31.10.2019
 */

package de.freese.spring.oauth2.client.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.web.client.RestTemplate;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public class OAuth2ClientRestApplication extends SpringBootServletInitializer
{
    /**
     * @param args String[]
     * @throws Exception Falls was schief geht.
     */
    @SuppressWarnings("resource")
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

    /**
     * @param env {@link Environment}
     * @return {@link RestTemplate}
     */
    @Bean
    public RestTemplate oAuth2RestTemplate(final Environment env)
    {
        // AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
        // ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();

        ResourceOwnerPasswordResourceDetails resourceDetails = new ResourceOwnerPasswordResourceDetails();
        resourceDetails.setClientId(env.getProperty("security.oauth2.client.clientId"));
        resourceDetails.setClientSecret(env.getProperty("security.oauth2.client.clientSecret"));
        resourceDetails.setAccessTokenUri(env.getProperty("security.oauth2.client.accessTokenUri"));
        resourceDetails.setUsername("admin");
        resourceDetails.setPassword("pw");

        OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(resourceDetails);

        // Validierung
        // oAuth2RestTemplate.getAccessToken();

        return oAuth2RestTemplate;
    }

    // /**
    // * Funktioniert nicht ... jeder User hat die ADMIN-Rolle !!!
    // *
    // * @param resource {@link OAuth2ProtectedResourceDetails}
    // * @param context {@link OAuth2ClientContext}
    // * @return {@link RestTemplate}
    // */
    // // @Bean
    // public RestTemplate oAuth2RestTemplate(final OAuth2ProtectedResourceDetails resource, final OAuth2ClientContext context)
    // {
    // OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(resource, context);
    //
    // oAuth2RestTemplate.setInterceptors(Arrays.asList(new BasicAuthenticationInterceptor("user", "pw")));
    //
    // // Validierung
    // // oAuth2RestTemplate.getAccessToken();
    //
    // return oAuth2RestTemplate;
    // }
}
