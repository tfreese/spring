// Created: 02.09.2018
package de.freese.spring.thymeleaf.config;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.web.filter.GenericFilterBean;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig
{
    /**
     * BasicAuthenticationEntryPoint liefert die volle HTML Fehler-Seite, dies ist bei REST nicht gewünscht.<br>
     * Aussedem wird die FilterChain weiter ausgeführt, wenn keine Credentials vorhanden sind.
     *
     * @author Thomas Freese
     */
    private static class RestAuthenticationEntryPoint extends BasicAuthenticationEntryPoint
    {
        /**
         * @see org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint#afterPropertiesSet()
         */
        @Override
        public void afterPropertiesSet()
        {
            setRealmName("Tommy");

            super.afterPropertiesSet();
        }

        /**
         * @see org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint#commence(javax.servlet.http.HttpServletRequest,
         *      javax.servlet.http.HttpServletResponse, org.springframework.security.core.AuthenticationException)
         */
        @Override
        public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException)
            throws IOException
        {
            response.addHeader("WWW-Authenticate", "Basic realm=" + getRealmName());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            PrintWriter writer = response.getWriter();
            writer.println("HTTP Status 401 - " + authException.getMessage());
        }
    }
    /**
     * @author Thomas Freese
     */
    @Configuration
    @Order(1)
    public static class RestSecurity extends WebSecurityConfigurerAdapter
    {
        /**
         *
         */
        @Resource
        private AuthenticationEntryPoint authenticationEntryPoint;
        /**
        *
        */
        @Resource
        private PreAuthenticatedAuthenticationProvider myTokenPreauthAuthProvider;
        /**
        *
        */
        @Resource
        private PasswordEncoder passwordEncoder;
        // /**
        // *
        // */
        // @Resource
        // private UserCache userCache;
        /**
        *
        */
        @Resource
        private RememberMeServices rememberMeServices;
        /**
        *
        */
        @Resource
        private UserDetailsService userDetailsService;

        /**
         * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#authenticationManagerBean()
         */
        @Override
        @Bean(name = "authenticationManagerRest")
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
            // DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
            // daoAuthenticationProvider.setUserDetailsService(userDetailsService());
            // daoAuthenticationProvider.setPasswordEncoder(this.passwordEncoder);
            // daoAuthenticationProvider.setUserCache(this.userCache);

            // @formatter:off
            auth
                .eraseCredentials(true)
                .userDetailsService(userDetailsService())
                .passwordEncoder(this.passwordEncoder)
                .and()
                //.authenticationProvider(daoAuthenticationProvider)
                .authenticationProvider(this.myTokenPreauthAuthProvider)
                ;
            // @formatter:on
        }

        /**
         * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
         */
        @Override
        protected void configure(final HttpSecurity httpSecurity) throws Exception
        {
            // Beispiel: https://developer.okta.com/blog/2018/07/30/10-ways-to-secure-spring-boot
            // http.requiresChannel().anyRequest().requiresSecure();
            //
            // http.requiresChannel()
            // .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
            // .requiresSecure();

            // @formatter:off
            httpSecurity
                //.anonymous().disable()
                .addFilterBefore(myTokenFilterRest(), RequestHeaderAuthenticationFilter.class)
                .authenticationProvider(this.myTokenPreauthAuthProvider)
                .antMatcher("/rest/**") // Nur auf den /rest Pfad beschränken.
                    .authorizeRequests()
                        .anyRequest().authenticated()// Alle HTTP Methoden zulässig.
                .and()
                    .httpBasic().authenticationEntryPoint(this.authenticationEntryPoint)
                .and()
                    //.formLogin().disable()
                    .csrf().disable() // .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .exceptionHandling()
                //.and()
                    //.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // REST-Services brauchen keine Session.
                .and()
                    .rememberMe()
                        .rememberMeServices(this.rememberMeServices)
                        .key("remember-me")
                        .tokenValiditySeconds(10 * 60) // 10 Minuten Gültigkeit
                 ;
                // Weitere Berechtigungen übernimmt die WebAppSecurity.
            // @formatter:on
        }

        /**
         * @return {@link GenericFilterBean}
         *
         * @throws Exception Falls was schief geht.
         */
        // @Bean // Mit @Bean funktionieren die REST-Services nicht mehr !
        public GenericFilterBean myTokenFilterRest() throws Exception
        {
            // RequestHeaderAuthenticationFilter filter = new RequestHeaderAuthenticationFilter();
            // filter.setPrincipalRequestHeader("my-token");
            // filter.setExceptionIfHeaderMissing(false); // Damit keine konkrete Fehlermeldung ausgegeben wird.
            // filter.setCheckForPrincipalChanges(true);
            // filter.setInvalidateSessionOnPrincipalChange(true);

            MyTokenRequestHeaderAuthenticationFilter filter = new MyTokenRequestHeaderAuthenticationFilter();
            filter.setAuthenticationManager(authenticationManagerBean());

            // MyTokenBasicAuthAuthenticationFilter filter = new MyTokenBasicAuthAuthenticationFilter(authenticationManagerBean());

            filter.afterPropertiesSet();

            return filter;
        }

        /**
         * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#userDetailsService()
         */
        @Override
        protected UserDetailsService userDetailsService()
        {
            return this.userDetailsService;
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

    /**
     * @author Thomas Freese
     */
    @Configuration
    @Order(2)
    public static class WebAppSecurity extends WebSecurityConfigurerAdapter
    {
        /**
        *
        */
        @Resource
        private PreAuthenticatedAuthenticationProvider myTokenPreauthAuthProvider;
        /**
        *
        */
        @Resource
        private PasswordEncoder passwordEncoder;
        /**
        *
        */
        @Resource
        private RememberMeServices rememberMeServices;
        // /**
        // *
        // */
        // @Resource
        // private UserCache userCache;
        /**
        *
        */
        @Resource
        private UserDetailsService userDetailsService;

        /**
         * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#authenticationManagerBean()
         */
        @Override
        @Bean(name = "authenticationManagerWeb")
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
            // DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
            // daoAuthenticationProvider.setUserDetailsService(userDetailsService());
            // daoAuthenticationProvider.setPasswordEncoder(this.passwordEncoder);
            // daoAuthenticationProvider.setUserCache(this.userCache);

            // @formatter:off
            auth
                .eraseCredentials(true)
                .userDetailsService(userDetailsService())
                .passwordEncoder(this.passwordEncoder)
                .and()
                    //.authenticationProvider(daoAuthenticationProvider)
                    .authenticationProvider(this.myTokenPreauthAuthProvider)

//                .inMemoryAuthentication()
//                .withUser("admin").password("{noop}admin").roles("ADMIN","USER")
//                .and()
//                .withUser("user").password("{noop}user").roles("USER")
                ;
            // @formatter:on
        }

        /**
         * Berechtigungen auf URL-Pfade.
         *
         * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
         */
        @Override
        protected void configure(final HttpSecurity httpSecurity) throws Exception
        {
            // Beispiel: https://developer.okta.com/blog/2018/07/30/10-ways-to-secure-spring-boot
            // http.requiresChannel().anyRequest().requiresSecure();
            //
            // http.requiresChannel()
            // .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
            // .requiresSecure();

            // @formatter:off
            httpSecurity
                //.anonymous().disable()
                .addFilterBefore(myTokenFilterWeb(), RequestHeaderAuthenticationFilter.class)
                .authenticationProvider(this.myTokenPreauthAuthProvider)
                .authorizeRequests()
                    .antMatchers("/", "/index", "/createError", "/login/**", "/actuator/**").permitAll()
                    //.antMatchers(HttpMethod.GET,"/admin/**").hasRole("ADMIN")
                    .antMatchers("/web/**").authenticated()
                    .anyRequest().denyAll()
                .and()
                    //.httpBasic().disable()
                    .formLogin()
                        .permitAll()
                        .loginPage("/login")
                        .failureUrl("/login?error=1")
                        .loginProcessingUrl("/authenticate") // Führt den Login durch
                        .defaultSuccessUrl("/web/person/personList") // Aufruf bei erfolgreichem Login
                .and()
                    .logout()
                        .permitAll()
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .invalidateHttpSession(true)
                        // .logoutUrl("/logout")
                        .logoutSuccessUrl("/index?logout")
                .and()
                    .csrf().disable() // .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .exceptionHandling()
                .and()
                    .rememberMe()
                        .rememberMeServices(this.rememberMeServices)
                        .key("remember-me")
                        .tokenValiditySeconds(10 * 60) // 10 Minuten Gültigkeit
                        //.tokenRepository(new InMemoryTokenRepositoryImpl())
                ;
            // @formatter:on
        }

        /**
         * Hier nur Web-Resourcen deklarieren.
         *
         * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.WebSecurity)
         */
        @Override
        public void configure(final WebSecurity webSecurity)
        {
            // @formatter:off
            webSecurity
                .ignoring()
                .antMatchers("/favicon.ico", "/manifest.appcache", "/css/**", "/js/**", "/images/**");
            // @formatter:on
        }

        /**
         * @return {@link GenericFilterBean}
         *
         * @throws Exception Falls was schief geht.
         */
        // @Bean // Mit @Bean funktionieren die REST-Services nicht mehr !
        public GenericFilterBean myTokenFilterWeb() throws Exception
        {
            // RequestHeaderAuthenticationFilter filter = new RequestHeaderAuthenticationFilter();
            // filter.setPrincipalRequestHeader("my-token");
            // filter.setExceptionIfHeaderMissing(false); // Damit keine konkrete Fehlermeldung ausgegeben wird.
            // filter.setCheckForPrincipalChanges(true);
            // filter.setInvalidateSessionOnPrincipalChange(true);

            MyTokenRequestHeaderAuthenticationFilter filter = new MyTokenRequestHeaderAuthenticationFilter();
            filter.setAuthenticationManager(authenticationManagerBean());

            // MyTokenBasicAuthAuthenticationFilter filter = new MyTokenBasicAuthAuthenticationFilter(authenticationManagerBean());

            filter.afterPropertiesSet();

            return filter;
        }

        /**
         * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#userDetailsService()
         */
        @Override
        protected UserDetailsService userDetailsService()
        {
            return this.userDetailsService;
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

    /**
     * {@link AccessDecisionVoter} prüfen die Authentifizierung nach bestimmten Vorgaben, z.B. Expression, Roles usw.<br>
     * HttpSecurity#accessDecisionManager(accessDecisionManager());<br>
     * <br>
     * Beispiel: Die Rolle 'USER' darf sich nur an geraden Minuten anmelden (MinuteBasedVoter):<br>
     *
     * <pre>
     * <code>
     * public int vote(Authentication authentication, Object object, Collection collection)
     * {
     *      return authentication.getAuthorities().stream()
     *          .map(GrantedAuthority::getAuthority)
     *          .filter(r -> "ROLE_USER".equals(r) && LocalDateTime.now().getMinute() % 2 != 0)
     *          .findAny()
     *          .map(s -> ACCESS_DENIED)
     *          .orElseGet(() -> ACCESS_ABSTAIN);
     * }
     *</code>
     * </pre>
     *
     * @return {@link AccessDecisionManager}
     *
     * @see {@link https://www.baeldung.com/spring-security-custom-voter}
     */
    // @Bean
    // public AccessDecisionManager accessDecisionManager()
    // {
//        // @formatter:off
//        List<AccessDecisionVoter<? extends Object>> decisionVoters =
//                Arrays.asList(
//                        new WebExpressionVoter(),
//                        new RoleVoter(),
//                        new AuthenticatedVoter(),
//                        new MinuteBasedVoter());
//
//        // @formatter:on
    // return new UnanimousBased(decisionVoters);
    // }

    /**
     * @return {@link AuthenticationEntryPoint}
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint()
    {
        RestAuthenticationEntryPoint authenticationEntryPoint = new RestAuthenticationEntryPoint();

        return authenticationEntryPoint;
    }

    /**
     * @param auds {@link AuthenticationUserDetailsService}
     *
     * @return {@link PreAuthenticatedAuthenticationProvider}
     */
    @Bean
    public PreAuthenticatedAuthenticationProvider myTokenPreauthAuthProvider(final AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> auds)
    {
        PreAuthenticatedAuthenticationProvider preauthAuthProvider = new PreAuthenticatedAuthenticationProvider();
        preauthAuthProvider.setPreAuthenticatedUserDetailsService(auds);

        return preauthAuthProvider;
    }

    /**
     * @param userDetailsService {@link UserDetailsService}
     *
     * @return {@link UserDetailsByNameServiceWrapper}
     */
    @Bean
    public UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken> myTokenUserDetailsServiceWrapper(final UserDetailsService userDetailsService)
    {
        UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken> wrapper = new UserDetailsByNameServiceWrapper<>();
        wrapper.setUserDetailsService(userDetailsService);

        return wrapper;
    }

    /**
     * @return {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder()
    {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        return bCryptPasswordEncoder;
    }

    /**
     * @param userDetailsService {@link UserDetailsService}
     *
     * @return {@link RememberMeServices}
     */
    @Bean
    public RememberMeServices rememberMeService(final UserDetailsService userDetailsService)
    {
        return new TokenBasedRememberMeServices("remember-me", userDetailsService);
    }

    /**
     * Liefert den {@link UserCache} für den {@link AbstractUserDetailsAuthenticationProvider}.
     *
     * @return {@link UserCache}
     *
     * @throws Exception Falls was schief geht.
     */
    @Bean
    public UserCache userCache() throws Exception
    {
        ConcurrentMapCacheFactoryBean bean = new ConcurrentMapCacheFactoryBean();
        bean.setAllowNullValues(false);
        bean.afterPropertiesSet();

        SpringCacheBasedUserCache userCache = new SpringCacheBasedUserCache(bean.getObject());

        return userCache;
    }

    /**
     * "{noop}PSW"= verhindert Meldungen wie "There is no PasswordEncoder mapped for the id "null""<br>
     * auth<br>
     * .inMemoryAuthentication()<br>
     * .withUser("admin").password("{noop}admin1").roles("ADMIN","USER")<br>
     * .and()<br>
     * .withUser("user").password("{noop}user1").roles("USER");<br>
     *
     * @param passwordEncoder {@link PasswordEncoder}
     * @param userCache {@link UserCache}
     *
     * @return {@link UserDetailsService}
     *
     * @throws Exception Falls was schief geht.
     */
    @Bean
    public UserDetailsService userDetailsService(final PasswordEncoder passwordEncoder, final UserCache userCache) throws Exception
    {
        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
        userDetailsManager.createUser(User.withUsername("admin").password(passwordEncoder.encode("pw")).roles("ADMIN", "USER").build());
        userDetailsManager.createUser(User.withUsername("user").password(passwordEncoder.encode("pw")).roles("USER").build());
        userDetailsManager.createUser(User.withUsername("invalid").password(passwordEncoder.encode("pw")).roles("OTHER").build());

        UserDetailsService userDetailsService = userDetailsManager;

        // CachingUserDetailsService erzeugen, erzeugt Fehler bei den Tests !
        // Constructor<CachingUserDetailsService> constructor = ClassUtils.getConstructorIfAvailable(CachingUserDetailsService.class, UserDetailsService.class);
        //
        // if (constructor == null)
        // {
        // constructor = CachingUserDetailsService.class.getDeclaredConstructor(UserDetailsService.class);
        // }
        //
        // if (constructor != null)
        // {
        // CachingUserDetailsService cachingUserDetailsService = BeanUtils.instantiateClass(constructor, userDetailsManager);
        // cachingUserDetailsService.setUserCache(userCache);
        //
        // userDetailsService = cachingUserDetailsService;
        // }

        return userDetailsService;
    }
}
