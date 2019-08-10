/**
 * Created: 25.09.2018
 */

package de.freese.spring.oauth.authorisation.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
    /**
     * Erstellt ein neues {@link WebSecurityConfig} Object.
     */
    public WebSecurityConfig()
    {
        super();
    }

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#authenticationManagerBean()
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception
    {
        return super.authenticationManagerBean();
    }

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder)
     */
    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception
    {
        // @formatter:off
        auth//.jdbcAuthentication().userCache(userCache
            .eraseCredentials(true)
            .userDetailsService(userDetailsService())
            .passwordEncoder(passwordEncoder())
        ;
        // @formatter:on
    }

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception
    {
        // @formatter:off
        http
            .anonymous().disable()
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/auth/rest/**").authenticated() // Nur auf den /rest Pfad beschränken.
                .anyRequest().denyAll()
            .and()
                .formLogin().disable()
                .httpBasic().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)

//            .antMatcher("/auth/rest/**")
//                .authorizeRequests()
//                    .anyRequest().authenticated()// Alle HTTP Methoden zulässig.
        ;
        // @formatter:on
    }

    /**
     * @return {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder()
    {
        Pbkdf2PasswordEncoder pbkdf2passwordEncoder = new Pbkdf2PasswordEncoder("mySecret");
        pbkdf2passwordEncoder.setAlgorithm(SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA512);
        pbkdf2passwordEncoder.setEncodeHashAsBase64(false);

        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("BCRYPT", new BCryptPasswordEncoder(10));
        encoders.put("PBKDF2", pbkdf2passwordEncoder);
        encoders.put("NOOP", new PasswordEncoder()
        {
            @Override
            public String encode(final CharSequence rawPassword)
            {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(final CharSequence rawPassword, final String encodedPassword)
            {
                return rawPassword.toString().equals(encodedPassword);
            }
        });

        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("NOOP", encoders);
        // passwordEncoder.setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance());

        return passwordEncoder;
    }

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#userDetailsService()
     */
    @Override
    @Bean
    public UserDetailsService userDetailsService()
    {
        // "{bcrypt}" + passwordEncoder.encode("pw")
        // PasswordEncoder passwordEncoder = passwordEncoder();

        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
        userDetailsManager.createUser(User.withUsername("admin").password("{NOOP}pw").roles("ADMIN", "USER").build());
        userDetailsManager.createUser(User.withUsername("user").password("{NOOP}pw").roles("USER").build());

        UserDetailsService userDetailsService = userDetailsManager;

        return userDetailsService;
    }

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#userDetailsServiceBean()
     */
    @Override
    public UserDetailsService userDetailsServiceBean() throws Exception
    {
        return userDetailsService();
    }
}
