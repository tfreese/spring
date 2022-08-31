// Created: 02.09.2018
package de.freese.spring.thymeleaf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.web.filter.GenericFilterBean;

/**
 * <a href="https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter">spring-security-without-the-websecurityconfigureradapter</a>
 *
 * @author Thomas Freese
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig
{
    /**
     * @return {@link AuthenticationEntryPoint}
     */
    @Bean
    AuthenticationEntryPoint authenticationEntryPoint()
    {
        return new RestAuthenticationEntryPoint();
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
     * @param authenticationProviderPreAuthenticated {@link AuthenticationProvider}
     * @param authenticationProviderDao {@link AuthenticationProvider}
     *
     * @return {@link AuthenticationManager}
     */
    @Bean
    AuthenticationManager authenticationManager(final AuthenticationProvider authenticationProviderPreAuthenticated,
                                                final AuthenticationProvider authenticationProviderDao)
    {
        ProviderManager providerManager = new ProviderManager(authenticationProviderPreAuthenticated, authenticationProviderDao);
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
     * @param authenticationUserDetailsService {@link AuthenticationUserDetailsService}
     *
     * @return {@link AuthenticationProvider}
     */
    @Bean
    AuthenticationProvider authenticationProviderPreAuthenticated(final AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> authenticationUserDetailsService)
    {
        PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider = new PreAuthenticatedAuthenticationProvider();
        preAuthenticatedAuthenticationProvider.setPreAuthenticatedUserDetailsService(authenticationUserDetailsService);

        return preAuthenticatedAuthenticationProvider;
    }

    /**
     * @param userDetailsService {@link UserDetailsService}
     *
     * @return {@link AuthenticationUserDetailsService}
     */
    @Bean
    AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> authenticationUserDetailsService(final UserDetailsService userDetailsService)
    {
        UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken> wrapper = new UserDetailsByNameServiceWrapper<>();
        wrapper.setUserDetailsService(userDetailsService);

        return wrapper;
    }

    /**
     *
     */
    @Bean
    SecurityFilterChain filterChain(final HttpSecurity httpSecurity, final PreAuthenticatedAuthenticationProvider myTokenPreauthAuthProvider,
                                    final AuthenticationEntryPoint authenticationEntryPoint, final RememberMeServices rememberMeServices,
                                    final AuthenticationManager authenticationManager)
            throws Exception
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
                .addFilterBefore(myTokenFilter(authenticationManager), RequestHeaderAuthenticationFilter.class)
                .authenticationProvider(myTokenPreauthAuthProvider)
//                .antMatcher("/rest/**") // Nur auf den /rest Pfad beschränken.
//                    .authorizeRequests()
//                    .anyRequest().authenticated()// Alle HTTP Methoden zulässig.
                .authorizeRequests()
                    .antMatchers("/", "/index", "/createError", "/login/**", "/actuator/**").permitAll()
                    //.antMatchers(HttpMethod.GET,"/admin/**").hasRole("ADMIN")
                    .antMatchers("/web/**").authenticated()
                    .antMatchers("/rest/**").authenticated()
                    .anyRequest().denyAll()
                .and()
                    //.httpBasic().disable()
                    .httpBasic().authenticationEntryPoint(authenticationEntryPoint)
                .and()
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
                //.and()
                    //.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // REST-Services brauchen keine Session.
                .and()
                    .rememberMe()
                    .rememberMeServices(rememberMeServices)
                    .key("remember-me")
                    .tokenValiditySeconds(10 * 60) // 10 Minuten Gültigkeit
                ;
        // @formatter:on

        return httpSecurity.build();
    }

    /**
     * @param authenticationManager {@link AuthenticationManager}
     *
     * @return {@link GenericFilterBean}
     *
     * @throws Exception Falls was schiefgeht.
     */
    // @Bean
    // Mit @Bean funktionieren die REST-Services nicht mehr !
    GenericFilterBean myTokenFilter(final AuthenticationManager authenticationManager) throws Exception
    {
        // RequestHeaderAuthenticationFilter filter = new RequestHeaderAuthenticationFilter();
        // filter.setPrincipalRequestHeader("my-token");
        // filter.setExceptionIfHeaderMissing(false); // Damit keine konkrete Fehlermeldung ausgegeben wird.
        // filter.setCheckForPrincipalChanges(true);
        // filter.setInvalidateSessionOnPrincipalChange(true);

        MyTokenRequestHeaderAuthenticationFilter filter = new MyTokenRequestHeaderAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager);

        // MyTokenBasicAuthAuthenticationFilter filter = new MyTokenBasicAuthAuthenticationFilter(authenticationManager);

        filter.afterPropertiesSet();

        return filter;
    }

    /**
     *
     */
    @Bean
    PreAuthenticatedAuthenticationProvider myTokenPreauthAuthProvider(final AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> auds)
    {
        PreAuthenticatedAuthenticationProvider preauthAuthProvider = new PreAuthenticatedAuthenticationProvider();
        preauthAuthProvider.setPreAuthenticatedUserDetailsService(auds);

        return preauthAuthProvider;
    }

    /**
     * @return {@link PasswordEncoder}
     */
    @Bean
    PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    /**
     * @param userDetailsService {@link UserDetailsService}
     *
     * @return {@link RememberMeServices}
     */
    @Bean
    RememberMeServices rememberMeService(final UserDetailsService userDetailsService)
    {
        return new TokenBasedRememberMeServices("remember-me", userDetailsService);
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
     * @throws Exception Falls was schiefgeht.
     */
    @Bean
    UserDetailsService userDetailsService(final PasswordEncoder passwordEncoder, final UserCache userCache) throws Exception
    {
        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
        userDetailsManager.createUser(User.withUsername("admin").passwordEncoder(passwordEncoder::encode).password("pw").roles("ADMIN", "USER").build());
        userDetailsManager.createUser(User.withUsername("user").passwordEncoder(passwordEncoder::encode).password("pw").roles("USER").build());
        userDetailsManager.createUser(User.withUsername("invalid").passwordEncoder(passwordEncoder::encode).password("pw").roles("OTHER").build());

        return userDetailsManager;

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
        // return cachingUserDetailsService;
        // }
    }

    /**
     * @return {@link WebSecurityCustomizer}
     */
    @Bean
    WebSecurityCustomizer webSecurityCustomizer()
    {
        // @formatter:off
        return webSecurity ->
                // Pfade ohne Sicherheitsprüfung.
                webSecurity.ignoring()
                        .antMatchers("/favicon.ico", "/manifest.appcache", "/css/**", "/js/**", "/images/**")
                ;
        // @formatter:on
    }
}
