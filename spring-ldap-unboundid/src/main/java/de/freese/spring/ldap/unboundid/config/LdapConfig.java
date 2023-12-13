package de.freese.spring.ldap.unboundid.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.server.UnboundIdContainer;
import org.springframework.security.ldap.userdetails.PersonContextMapper;

/**
 * @author Thomas Freese
 */
@Configuration
public class LdapConfig {
    @Bean
    LdapAuthenticationProvider authenticationProvider(final LdapAuthenticator authenticator) {
        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(authenticator);
        provider.setUserDetailsContextMapper(new PersonContextMapper());

        return provider;
    }

    @Bean
    BindAuthenticator authenticator(final BaseLdapPathContextSource contextSource) {
        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserDnPatterns(new String[]{"uid={0}, ou=people"});

        return authenticator;
    }

    @Bean
    BaseLdapPathContextSource contextSource(final UnboundIdContainer container, final @Value("${spring.ldap.base-dn}") String baseDn) {
        int port = container.getPort();

        return new DefaultSpringSecurityContextSource("ldap://localhost:" + port + "/" + baseDn);
    }

    @Bean
    UnboundIdContainer ldapContainer(final @Value("${spring.ldap.base-dn}") String baseDn) {
        UnboundIdContainer container = new UnboundIdContainer(baseDn, "classpath:users.ldif");
        container.setPort(0);

        return container;
    }
}
