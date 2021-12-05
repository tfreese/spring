package com.baeldung.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 *
 */
@EnableWebSecurity
public class DefaultSecurityConfig
{
    /**
     * @param http {@link HttpSecurity}
     *
     * @return {@link SecurityFilterChain}
     *
     * @throws Exception Falls was schief geht.
     */
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(final HttpSecurity http) throws Exception
    {
        http.authorizeRequests(authorizeRequests -> authorizeRequests.anyRequest().authenticated()).formLogin(withDefaults());

        return http.build();
    }

    /**
     * @return {@link UserDetailsService}
     */
    @Bean
    UserDetailsService users()
    {
        UserDetails user = User.withDefaultPasswordEncoder().username("admin").password("password").roles("USER").build();

        // Map<String, PasswordEncoder> encoders = new HashMap<>();
        // encoders.put("noop", new PasswordEncoder()
        // {
        // @Override
        // public String encode(final CharSequence rawPassword)
        // {
        // return rawPassword.toString();
        // }
        //
        // @Override
        // public boolean matches(final CharSequence rawPassword, final String encodedPassword)
        // {
        // return rawPassword.toString().equals(encodedPassword);
        // }
        // });
        //
        // PasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("noop", encoders);
        //
        // UserDetails user = User.builder().passwordEncoder(passwordEncoder::encode).username("admin").password("password").roles("USER").build();

        return new InMemoryUserDetailsManager(user);
    }
}
