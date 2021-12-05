package com.baeldung.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

/**
 *
 */
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig
{
    /**
     * @return {@link RSAKey}
     */
    private static RSAKey generateRsa()
    {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build();
    }

    /**
     * @return {@link KeyPair}
     */
    private static KeyPair generateRsaKey()
    {
        KeyPair keyPair;

        try
        {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(4096);
            keyPair = keyPairGenerator.generateKeyPair();
        }
        catch (Exception ex)
        {
            throw new IllegalStateException(ex);
        }

        return keyPair;
    }

    /**
     * @param http {@link HttpSecurity}
     *
     * @return {@link SecurityFilterChain}
     *
     * @throws Exception Falls was schief geht.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authServerSecurityFilterChain(final HttpSecurity http) throws Exception
    {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        return http.formLogin(Customizer.withDefaults()).build();
    }

    /**
     * @return {@link JWKSource}
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource()
    {
        RSAKey rsaKey = generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);

        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    /**
     * @return {@link ProviderSettings}
     */
    @Bean
    public ProviderSettings providerSettings()
    {
        return ProviderSettings.builder().issuer("http://127.0.0.1:9000").build();
    }

    /**
     * @return {@link RegisteredClientRepository}
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository()
    {
        // @formatter:off
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("articles-client")
                .clientSecret("{noop}secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/articles-client-oidc")
                .redirectUri("http://127.0.0.1:8080/authorized")
                .scope(OidcScopes.OPENID)
                .scope("articles.read")
                .build()
                ;
        // @formatter:on

        return new InMemoryRegisteredClientRepository(registeredClient);
    }
}