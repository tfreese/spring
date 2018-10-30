/**
 * Created: 25.09.2018
 */

package org.spring.oauth.jwt.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.spring.oauth.jwt.token.JwtTokenAuthenticationProvider;
import org.spring.oauth.jwt.token.JwtTokenFilter;
import org.spring.oauth.jwt.token.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

/**
 * @author Thomas Freese
 */
@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
    /**
     * BasicAuthenticationEntryPoint liefert die volle HTML Fehler-Seite, dies ist bei REST nicht gewünscht.<br>
     * Aussedem wird die FilterChain weiter ausgeführt, wenn keine Credentials vorhanden sind.
     *
     * @author Thomas Freese
     */
    private static class RestJwtAuthenticationEntryPoint extends BasicAuthenticationEntryPoint
    {
        /**
         * Erstellt ein neues {@link RestJwtAuthenticationEntryPoint} Object.
         */
        RestJwtAuthenticationEntryPoint()
        {
            super();
        }

        /**
         * @see org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint#afterPropertiesSet()
         */
        @Override
        public void afterPropertiesSet() throws Exception
        {
            setRealmName("Tommy");

            super.afterPropertiesSet();
        }

        /**
         * @see org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint#commence(javax.servlet.http.HttpServletRequest,
         *      javax.servlet.http.HttpServletResponse, org.springframework.security.core.AuthenticationException)
         */
        @Override
        public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authEx)
            throws IOException, ServletException
        {
            response.addHeader("WWW-Authenticate", "Basic realm=" + getRealmName());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

            @SuppressWarnings("resource")
            PrintWriter writer = response.getWriter();
            writer.println("HTTP Status 401 - " + authEx.getMessage());
        }
    }

    /**
     *
     */
    @Resource
    private JwtTokenProvider jwtTokenProvider = null;

    /**
     *
     */
    @Resource
    private UserCache userCache = null;

    /**
     * Erstellt ein neues {@link SecurityConfig} Object.
     */
    public SecurityConfig()
    {
        super();
    }

    /**
     * @return {@link AuthenticationEntryPoint}
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint()
    {
        RestJwtAuthenticationEntryPoint authenticationEntryPoint = new RestJwtAuthenticationEntryPoint();

        return authenticationEntryPoint;
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
        auth
            .eraseCredentials(false) // Sonst würden die Passwörter aus dem gecachetem UserDetails gelöscht werden -> kein weiterer Request wäre mehr möglich.
            .authenticationProvider(jwtAuthenticationProvider())
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
            .formLogin().disable()
            .httpBasic().disable()
            .authorizeRequests()
                .antMatchers("/jwt/users/login").permitAll()
                .antMatchers("/jwt/users/register").permitAll()
                .anyRequest().authenticated()
            .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
//                .apply(new JwtTokenFilterConfigurer(this.jwtTokenProvider))
                .addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        ;
        // @formatter:on
    }

    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.WebSecurity)
     */
    @Override
    public void configure(final WebSecurity web) throws Exception
    {
        // Allow swagger to be accessed without authentication
        web.ignoring().antMatchers("/v2/api-docs")//
                .antMatchers("/swagger-resources/**")//
                .antMatchers("/swagger-ui.html")//
                .antMatchers("/configuration/**")//
                .antMatchers("/webjars/**")//
                .antMatchers("/public")

                // Un-secure H2 Database (for testing purposes, H2 console shouldn't be unprotected in production)
                .and().ignoring().antMatchers("/h2-console/**/**");
        ;
    }

    /**
     * @return {@link AuthenticationProvider}
     */
    @Bean
    public AuthenticationProvider jwtAuthenticationProvider()
    {
        JwtTokenAuthenticationProvider jwtAuthenticationProvider = new JwtTokenAuthenticationProvider();
        jwtAuthenticationProvider.setUserDetailsService(userDetailsService());
        jwtAuthenticationProvider.setUserCache(this.userCache);
        jwtAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        jwtAuthenticationProvider.setTokenProvider(this.jwtTokenProvider);

        return jwtAuthenticationProvider;
    }

    /**
     * @return {@link Filter}
     * @throws Exception Falls was schief geht.
     */
    @Bean
    public Filter jwtTokenFilter() throws Exception
    {
        JwtTokenFilter jwtTokenFilter = new JwtTokenFilter();
        jwtTokenFilter.setAuthenticationManager(authenticationManager());
        jwtTokenFilter.setAuthenticationEntryPoint(authenticationEntryPoint());

        return jwtTokenFilter;
    }

    /**
     * @return {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder()
    {
        // String defaultIdForEncode = "bcrypt";
        String defaultIdForEncode = "noop";
        Map<String, PasswordEncoder> encoders = new HashMap<>();

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(10);

        Pbkdf2PasswordEncoder pbkdf2passwordEncoder = new Pbkdf2PasswordEncoder("mySecret");
        pbkdf2passwordEncoder.setAlgorithm(SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA512);
        pbkdf2passwordEncoder.setEncodeHashAsBase64(false);

        encoders.put("bcrypt", bCryptPasswordEncoder);
        encoders.put("pbkdf2", pbkdf2passwordEncoder);
        encoders.put("noop", NoOpPasswordEncoder.getInstance());
        encoders.put("sha256", new StandardPasswordEncoder("mySecret"));

        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder(defaultIdForEncode, encoders);
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
        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
        // userDetailsManager.createUser(User.withUsername("admin").password("{noop}pw").roles("ADMIN", "USER").build());
        // userDetailsManager.createUser(User.withUsername("user").password("{noop}pw").roles("USER").build());

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
