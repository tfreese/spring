// Created: 02.09.2018
package de.freese.spring.thymeleaf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
@EnableMethodSecurity(securedEnabled = true)
@SuppressWarnings("java:S6437")
public class SecurityConfig {
    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    AuthenticationManager authenticationManager(final AuthenticationProvider authenticationProviderPreAuthenticated, final AuthenticationProvider authenticationProviderDao) {
        final ProviderManager providerManager = new ProviderManager(authenticationProviderPreAuthenticated, authenticationProviderDao);
        // providerManager.setMessageSource(applicationContext); // Done automatically.
        providerManager.setEraseCredentialsAfterAuthentication(true);

        return providerManager;
    }

    /**
     * For Username/Password Login.<br>
     * UserController.login(String, String)<br>
     */
    @Bean
    AuthenticationProvider authenticationProviderDao(final PasswordEncoder passwordEncoder, final UserDetailsService userDetailsService, final UserCache userCache) {
        final DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        // authenticationProvider.setMessageSource(applicationContext); // Done automatically.
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        // Böse Falle !
        // Der UserCache im AuthenticationProvider behält die UserDetails der User.
        // Bei diesen werden aber die Passwörter aus Sicherheitsgründen im ProviderManager entfernt.
        // Dadurch ist ein 2. Login dann nicht mehr möglich, es folgt NullPointer wegen UserDetails.getPassword = null
        // authenticationProvider.setUserCache(userCache);

        // Dieses Problem könnte behoben werden, indem nur der UserName und nicht das User-Object verwendet wird.
        // Dann kann aber nicht der User in die Controller-Methode übergeben werden.
        // -> ..., @AuthenticationPrincipal final UserDetails user)
        // authenticationProvider.setForcePrincipalAsString(true);

        // Lösung: UserDetailsService mit Cache in der Methode #loadUserByUsername(String)

        return authenticationProvider;
    }

    @Primary
    @Bean
    AuthenticationProvider authenticationProviderPreAuthenticated(final AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> authenticationUserDetailsService) {
        final PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider = new PreAuthenticatedAuthenticationProvider();
        preAuthenticatedAuthenticationProvider.setPreAuthenticatedUserDetailsService(authenticationUserDetailsService);

        return preAuthenticatedAuthenticationProvider;
    }

    @Bean
    AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> authenticationUserDetailsService(final UserDetailsService userDetailsService) {
        final UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken> wrapper = new UserDetailsByNameServiceWrapper<>();
        wrapper.setUserDetailsService(userDetailsService);

        return wrapper;
    }

    @Bean
    SecurityFilterChain filterChain(final HttpSecurity httpSecurity, final PreAuthenticatedAuthenticationProvider myTokenPreauthAuthProvider,
                                    final AuthenticationEntryPoint authenticationEntryPoint, final RememberMeServices rememberMeServices,
                                    final AuthenticationManager authenticationManager) throws Exception {
        // Beispiel: https://developer.okta.com/blog/2018/07/30/10-ways-to-secure-spring-boot
        // http.requiresChannel().anyRequest().requiresSecure();
        //
        // http.requiresChannel()
        // .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
        // .requiresSecure();

        httpSecurity
                // .anonymous(customizer -> customizer
                //         .disable()
                // )
                .addFilterBefore(myTokenFilter(authenticationManager), RequestHeaderAuthenticationFilter.class)
                .authenticationProvider(myTokenPreauthAuthProvider)
                .authorizeHttpRequests(customizer -> customizer
                        .requestMatchers("/", "/index", "/createError", "/login/**", "/actuator/**", "/favicon.ico", "/manifest.appcache", "/css/**", "/js/**", "/images/**")
                        .permitAll()
                        //.requestMatchers(HttpMethod.GET,"/admin/**").hasRole("ADMIN")
                        .requestMatchers("/web/**").authenticated()
                        .requestMatchers("/rest/**").authenticated()
                        .anyRequest().denyAll() //.authenticated()// Alle HTTP Methoden zulässig.
                )
                .httpBasic(customizer -> customizer
                        // .disable()
                        .authenticationEntryPoint(authenticationEntryPoint)
                )
                .formLogin(customizer -> customizer
                        .permitAll()
                        .loginPage("/login")
                        .failureUrl("/login?error=1")
                        .loginProcessingUrl("/authenticate") // Login Page
                        .defaultSuccessUrl("/web/person/personList") // After successful Login.
                )
                .logout(customizer -> customizer
                        .permitAll()
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .invalidateHttpSession(true)
                        // .logoutUrl("/logout")
                        .logoutSuccessUrl("/index?logout")
                )
                // .csrf(customizer -> customizer
                //         // .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                //         .disable()
                // )
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(Customizer.withDefaults())
                // .sessionManagement(customizer -> customizer
                //         .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // REST-Services don't need a Session.
                // )
                .rememberMe(customizer -> customizer
                        .rememberMeServices(rememberMeServices)
                        .key("remember-me")
                        .tokenValiditySeconds(10 * 60) // 10 Minutes valid.
                )
        ;

        return httpSecurity.build();
    }

    // @Bean
    // With this @Bean the REST-Services won't work!
    GenericFilterBean myTokenFilter(final AuthenticationManager authenticationManager) {
        // final RequestHeaderAuthenticationFilter filter = new RequestHeaderAuthenticationFilter();
        // filter.setPrincipalRequestHeader("my-token");
        // filter.setExceptionIfHeaderMissing(false); // No Exception.
        // filter.setCheckForPrincipalChanges(true);
        // filter.setInvalidateSessionOnPrincipalChange(true);

        final RequestHeaderAuthenticationFilter filter = MyTokenRequestHeaderAuthenticationFilter.of("my-token");
        filter.setAuthenticationManager(authenticationManager);

        // final MyTokenBasicAuthAuthenticationFilter filter = new MyTokenBasicAuthAuthenticationFilter(authenticationManager);

        filter.afterPropertiesSet();

        return filter;
    }

    @Bean
    PreAuthenticatedAuthenticationProvider myTokenPreauthAuthProvider(final AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> auds) {
        final PreAuthenticatedAuthenticationProvider preauthAuthProvider = new PreAuthenticatedAuthenticationProvider();
        preauthAuthProvider.setPreAuthenticatedUserDetailsService(auds);

        return preauthAuthProvider;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    RememberMeServices rememberMeService(final UserDetailsService userDetailsService) {
        return new TokenBasedRememberMeServices("remember-me", userDetailsService);
    }

    /**
     * "{noop}PSW"= avoid Messages like "There is no PasswordEncoder mapped for the id "null""<br>
     * auth<br>
     * .inMemoryAuthentication()<br>
     * .withUser("admin").password("{noop}admin1").roles("ADMIN","USER")<br>
     * .and()<br>
     * .withUser("user").password("{noop}user1").roles("USER");<br>
     */
    @Bean
    UserDetailsService userDetailsService(final PasswordEncoder passwordEncoder, final UserCache userCache) {
        final InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
        userDetailsManager.createUser(User.withUsername("admin").passwordEncoder(passwordEncoder::encode).password("pw").roles("ADMIN", "USER").build());
        userDetailsManager.createUser(User.withUsername("user").passwordEncoder(passwordEncoder::encode).password("pw").roles("USER").build());
        userDetailsManager.createUser(User.withUsername("invalid").passwordEncoder(passwordEncoder::encode).password("pw").roles("OTHER").build());

        return userDetailsManager;

        // Create CachingUserDetailsService, causes Errors in the Tests!
        // final Constructor<CachingUserDetailsService> constructor = ClassUtils.getConstructorIfAvailable(CachingUserDetailsService.class, UserDetailsService.class);
        //
        // if (constructor == null) {
        // constructor = CachingUserDetailsService.class.getDeclaredConstructor(UserDetailsService.class);
        // }
        //
        // if (constructor != null) {
        // final CachingUserDetailsService cachingUserDetailsService = BeanUtils.instantiateClass(constructor, userDetailsManager);
        // cachingUserDetailsService.setUserCache(userCache);
        //
        // return cachingUserDetailsService;
        // }
    }

    //    @Bean
    //    WebSecurityCustomizer webSecurityCustomizer() {
    //    return webSecurity ->
    //            // Pfade ohne Sicherheitsprüfung.
    //            webSecurity.ignoring()
    //                    .requestMatchers(, , )
    //            ;
    //    }
}
