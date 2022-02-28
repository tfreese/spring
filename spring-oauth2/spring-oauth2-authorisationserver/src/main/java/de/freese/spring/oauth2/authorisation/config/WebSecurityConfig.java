// Created: 25.09.2018
package de.freese.spring.oauth2.authorisation.config;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author Thomas Freese
 */
@Configuration
@Order(1)
@EnableWebSecurity
class WebSecurityConfig
{
    /**
     *
     */
    @Resource
    private UserDetailsService myUserDetailsService;
    /**
     *
     */
    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * @param authenticationProviderDao {@link AuthenticationProvider}
     *
     * @return {@link AuthenticationManager}
     */
    @Bean
    AuthenticationManager authenticationManager(final AuthenticationProvider authenticationProviderDao)
    {
        ProviderManager providerManager = new ProviderManager(authenticationProviderDao);
        // providerManager.setMessageSource(applicationContext); // Wird automatisch gemacht.
        providerManager.setEraseCredentialsAfterAuthentication(true);

        return providerManager;
    }

    /**
     * Für Username/Password Login.<br>
     * UserController.login(String, String)<br>
     *
     * @param passwordEncoder {@link PasswordEncoder}
     * @param userDetailsService {@link UserDetailsService}
     * @param userCache {@link UserCache}
     *
     * @return {@link AuthenticationProvider}
     */
    @Bean
    AuthenticationProvider authenticationProviderDao(final PasswordEncoder passwordEncoder, final UserDetailsService userDetailsService,
                                                     final UserCache userCache)
    {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        // authenticationProvider.setMessageSource(applicationContext); // Wird automatisch gemacht.
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        authenticationProvider.setUserDetailsService(userDetailsService);

        // Böse Falle !
        // Der UserCache im AuthenticationProvider behält die UserDetails der User.
        // Bei diesen werden aber die Passwörter aus Sicherheitsgründen im ProviderManager entfernt.
        // Dadurch ist ein 2. Login dann nicht mehr möglich -> NullPointer wegen UserDetails.getPassword = null
        // authenticationProvider.setUserCache(userCache);

        // Dieses Problem könnte behoben werden, indem nur der UserName und nicht das User-Object verwendet wird.
        // Dann kann aber nicht der User in die Controller-Methode übergeben werden.
        // -> ..., @AuthenticationPrincipal final UserDetails user)
        // authenticationProvider.setForcePrincipalAsString(true);

        // Lösung: UserDetailsService mit Cache in der Methode #loadUserByUsername(String)

        return authenticationProvider;
    }

    /**
     * @param http {@link HttpSecurity}
     *
     * @return {@link SecurityFilterChain}
     *
     * @throws Exception Falls was schief geht
     */
    @Bean
    SecurityFilterChain filterChain(final HttpSecurity http) throws Exception
    {
        // @formatter:off
        http.authorizeRequests()
                .antMatchers("/login").permitAll()
                .antMatchers("/oauth/authorize").permitAll()
                .antMatchers("/oauth/token/revokeById/**").permitAll()
//            .antMatchers("/tokens/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().permitAll()
                .and()
                .csrf().disable()
                .anonymous().disable()
                ;
        // @formatter:on);

        return http.build();
    }
}
