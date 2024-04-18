package de.freese.spring.ldap.unboundid.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Bean
    AuthenticationManager authenticationManager(final AuthenticationProvider authenticationProviderDao) {
        final ProviderManager providerManager = new ProviderManager(authenticationProviderDao);
        // providerManager.setMessageSource(applicationContext); // Wird automatisch gemacht.
        providerManager.setEraseCredentialsAfterAuthentication(true);

        return providerManager;
    }

    @Bean
    SecurityFilterChain filterChain(final HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeHttpRequests(customizer -> customizer
                        .anyRequest()
                        .fullyAuthenticated()
                )
                .formLogin(Customizer.withDefaults())
        ;

        return httpSecurity.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        final Pbkdf2PasswordEncoder pbkdf2passwordEncoder = new Pbkdf2PasswordEncoder("mySecret", 16, 310_000,
                Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA512);
        pbkdf2passwordEncoder.setEncodeHashAsBase64(false);

        final Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("pbkdf2", pbkdf2passwordEncoder);
        encoders.put("bcrypt", new BCryptPasswordEncoder(10));
        encoders.put("", new PasswordEncoder() {
            @Override
            public String encode(final CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }
        });

        return new DelegatingPasswordEncoder("", encoders);
    }
}
