// Created: 19.01.2018
package de.freese.spring.security.rest.config;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

/**
 * <pre>
 * SecurityContext context = SecurityContextHolder.getContext();
 * Authentication authentication = context.getAuthentication();
 * </pre>
 *
 * <pre>
 * &#64;RequestMapping("/foo")
 * publicv String foo(@AuthenticationPrincipal User user)
 * { ... // do stuff with user }
 * </pre>
 *
 * <pre>
 * &#64;RequestMapping("/foo")
 * public String foo(Principal principal) {
 * Authentication authentication = (Authentication) principal;
 * User = (User) authentication.getPrincipal();
 * ... // do stuff with user}
 * </pre>
 *
 * <br>
 * Processing Secure Methods Asynchronously:<br>
 *
 * <pre>
 * &#64;Configuration public class ApplicationConfiguration extends AsyncConfigurerSupport
 * &#64;Override
 * public Executor getAsyncExecutor() {
 *  return new DelegatingSecurityContextExecutorService(Executors.newFixedThreadPool(5));
 *  }
 * }
 * </pre>
 *
 * https://chclaus.de/2016/02/09/spring-boot-security-authenticate-programmatically/<br>
 * https://jaxenter.com/rest-api-spring-java-8-112289.html<br>
 * https://github.com/spring-projects/spring-data-examples/tree/master/rest/security<br>
 * http://www.learningthegoodstuff.com/2014/12/spring-security-pre-authentication-and.html<br>
 *
 * @author Thomas Freese
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /**
     * Username + Password<br>
     * curl http://localhost:10000/spring-security/rest/admin -u admin:admin1
     *
     * @author Thomas Freese
     */
    @Configuration
    // @Order(1)
    @Profile("login")
    public static class LoginConfigurationAdapter extends WebSecurityConfigurerAdapter {
        /**
         *
         */
        @Resource
        private UserDetailsService userDetailsService;

        /**
         * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.WebSecurity)
         */
        @Override
        public void configure(final WebSecurity web) throws Exception {
            // @formatter:off
            web
                .ignoring()
                .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");
            // @formatter:on
        }

        /**
         * @param auth {@link AuthenticationManagerBuilder}
         *
         * @throws Exception Falls was schief geht.
         */
        @Resource
        public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
            DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
            daoAuthenticationProvider.setUserDetailsService(this.userDetailsService);

            auth.authenticationProvider(daoAuthenticationProvider);
        }

        /**
         * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
         */
        @Override
        protected void configure(final HttpSecurity http) throws Exception {
            // @formatter:off
            http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET,"/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasAnyRole("ADMIN", "USER")// Alle HTTP Methoden zulässig.
                .antMatchers("/", "/auth").permitAll()
                .anyRequest().authenticated()
            .and()
                .httpBasic()
            .and()
                .csrf().disable(); // Wird bei REST nicht benötigt.
            // @formatter:on
        }
    }

    /**
     * Pre-Authentication, Token im HTTP-Header<br>
     * curl -H "iv-user: admin" http://localhost:10000/spring-security/rest/user
     *
     * @author Thomas Freese
     */
    @Configuration
    // @Order(2)
    @Profile("pre-auth")
    public static class PreAuthConfigurationAdapter extends WebSecurityConfigurerAdapter {
        /**
         *
         */
        @Resource
        private UserDetailsService userDetailsService;

        /**
         * @param auth {@link AuthenticationManagerBuilder}
         *
         * @throws Exception Falls was schief geht.
         */
        @Resource
        public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(preauthAuthProvider());
        }

        /**
         * @return {@link PreAuthenticatedAuthenticationProvider}
         */
        @Bean
        public PreAuthenticatedAuthenticationProvider preauthAuthProvider() {
            PreAuthenticatedAuthenticationProvider preauthAuthProvider = new PreAuthenticatedAuthenticationProvider();
            preauthAuthProvider.setPreAuthenticatedUserDetailsService(userDetailsServiceWrapper());

            return preauthAuthProvider;
        }

        /**
         * @return {@link UserDetailsByNameServiceWrapper}
         */
        @Bean
        public UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken> userDetailsServiceWrapper() {
            UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken> wrapper = new UserDetailsByNameServiceWrapper<>();
            wrapper.setUserDetailsService(this.userDetailsService);

            return wrapper;
        }

        /**
         * @return {@link RequestHeaderAuthenticationFilter}
         *
         * @throws Exception Falls was schief geht.
         */
        @Bean
        public RequestHeaderAuthenticationFilter webSealFilter() throws Exception {
            // WebSealRequestHeaderAuthenticationFilter filter = new WebSealRequestHeaderAuthenticationFilter();
            RequestHeaderAuthenticationFilter filter = new RequestHeaderAuthenticationFilter();
            filter.setPrincipalRequestHeader("iv-user");
            filter.setExceptionIfHeaderMissing(false); // Damit keine konkrete Fehlermeldung ausgegeben wird.
            filter.setCheckForPrincipalChanges(true);
            filter.setInvalidateSessionOnPrincipalChange(true);

            filter.setAuthenticationManager(authenticationManager());

            return filter;
        }

        /**
         * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
         */
        @Override
        protected void configure(final HttpSecurity http) throws Exception {
            // @formatter:off
            http
                .addFilterBefore(webSealFilter(), RequestHeaderAuthenticationFilter.class)
                .authenticationProvider(preauthAuthProvider())
                .authorizeRequests()
//                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET,"/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasAnyRole("ADMIN", "USER")// Alle HTTP Methoden zulässig.
                .antMatchers("/", "/auth").permitAll()
                .anyRequest().authenticated()
            .and()
                .csrf().disable(); // Wird bei REST nicht benötigt.
            // @formatter:on
        }
    }

    /**
     * Erzeugt eine neue Instanz von {@link SecurityConfig}.
     */
    public SecurityConfig() {
        super();
    }

    // /**
    // * @param auth {@link AuthenticationManagerBuilder}
    // * @throws Exception Falls was schief geht.
    // */
    // @Resource
    // public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception
    // {
    //        // @formatter:off
//        auth.inMemoryAuthentication()
//            .withUser("admin").password("admin1").roles("ADMIN","USER")
//            .and()
//            .withUser("user").password("user1").roles("USER");
//        // @formatter:on
    //
    // // auth.userDetailsService(userDetailsService());
    //
    // // .and().passwordEncoder(passwordEncoder());
    //
    // // auth.jdbcAuthentication().dataSource(dataSource)
    // // .usersByUsernameQuery(
    // // "select username,password, enabled from users where username=?")
    // // .authoritiesByUsernameQuery(
    // // "select username, role from user_roles where username=?");
    // }

    /**
     * @return {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        return bCryptPasswordEncoder;
    }

    /**
     * @return {@link UserDetailsService}
     */
    @Bean
    public UserDetailsService userDetailsService() {
        // "{noop}PSW"= verhindert Meldungen wie "There is no PasswordEncoder mapped for the id "null""
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("admin").password("{noop}admin1").roles("ADMIN", "USER").build());
        manager.createUser(User.withUsername("user").password("{noop}user1").roles("USER").build());

        return manager;
    }
}
