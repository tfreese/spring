// Created: 25.09.2018
package de.freese.spring.jwt.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import jakarta.servlet.Filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.Assert;

import de.freese.spring.jwt.token.JwtTokenProvider;
import de.freese.spring.jwt.token.nimbus.JwtTokenProviderNimbus;

/**
 * <a href="https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter">spring-security-without-the-websecurityconfigureradapter</a>
 *
 * @author Thomas Freese
 */
@Configuration
public class SecurityCommonConfig {
    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    /**
     * Für Username/Password Login.<br>
     * UserController.login(String, String)<br>
     */
    @Bean
    AuthenticationProvider authenticationProviderDao(final PasswordEncoder passwordEncoder, final UserDetailsService userDetailsService, final UserCache userCache) {
        final DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        // authenticationProvider.setMessageSource(applicationContext); // Wird automatisch gemacht.
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        authenticationProvider.setUserDetailsService(userDetailsService);

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

    @Bean
    SecurityFilterChain filterChain(final HttpSecurity httpSecurity, final Filter jwtRequestFilter, final AuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        // @formatter:off
        httpSecurity
                // .authorizeHttpRequests(customizer -> customizer.anyRequest().permitAll())
                // .anonymous(customizer -> customizer.disable()) // Jeder User muss angemeldet sein, beisst sich mit antMatchers("/users/login").permitAll()
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(customizer -> customizer
                    .requestMatchers("/users/login").permitAll()
                    //.requestMatchers("/users/register").hasRole("ADMIN")
                    .anyRequest().authenticated()
                )
                .exceptionHandling(customizer -> customizer.authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // .apply(new JwtTokenFilterConfigurer(this.jwtTokenProvider))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
            ;
        // @formatter:on

        return httpSecurity.build();
    }

    @Bean
    JwtTokenProvider jwtTokenUtils(@Value("${security.jwt.token.secret-key:secret-key}") final String secretKey,
                                   @Value("${security.jwt.token.expire-length:3600000}") final long validityInMilliseconds) {
        // final byte[] salt = KeyGenerators.secureRandom(16).generateKey();
        //
        // final PBEKeySpec keySpec = new PBEKeySpec(this.secretKey.toCharArray(), salt, 1024, 256);
        // final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        // final SecretKey secretKey = factory.generateSecret(keySpec);

        return new JwtTokenProviderNimbus(validityInMilliseconds, secretKey);
        //        return new JwtTokenProviderJson(validityInMilliseconds, secretKey);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        final Pbkdf2PasswordEncoder pbkdf2passwordEncoder = new Pbkdf2PasswordEncoder("mySecret", 16, 310_000,
                Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA512);
        pbkdf2passwordEncoder.setEncodeHashAsBase64(false);

        final Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("pbkdf2", pbkdf2passwordEncoder);
        encoders.put("bcrypt", new BCryptPasswordEncoder(10));
        // encoders.put("scrypt", new SCryptPasswordEncoder()); // Benötigt BountyCastle
        // encoders.put("argon2", new Argon2PasswordEncoder()); // Benötigt BountyCastle
        encoders.put("noop", new PasswordEncoder() {
            @Override
            public String encode(final CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }
        });

        return new DelegatingPasswordEncoder("noop", encoders);
    }

    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    UserDetailsService userDetailsService(final PasswordEncoder passwordEncoder) {
        final InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();

        userDetailsManager.createUser(User.withUsername("admin").passwordEncoder(passwordEncoder::encode).password("pass").roles("ADMIN", "USER").build());
        userDetailsManager.createUser(User.withUsername("user").passwordEncoder(passwordEncoder::encode).password("pass").roles("USER").build());

        // UserDetails kopieren, da bei ProviderManager.setEraseCredentialsAfterAuthentication(true)
        // das Password auf null gesetzt wird, kein zweiter Login mehr möglich, es folgt NullPointer
        // Siehe #userDetailsServiceJdbc().
        // Das Kopieren der UserDetails findet hier bereits im InMemoryUserDetailsManager#loadUserByUsername statt.

        return userDetailsManager;
    }

    @Bean
    @ConditionalOnBean(DataSource.class)
    UserDetailsService userDetailsServiceJdbc(final DataSource dataSource, final UserCache userCache) {
        final JdbcDaoImpl jdbcDao = new JdbcDaoImpl();
        jdbcDao.setDataSource(dataSource);
        jdbcDao.setUsersByUsernameQuery(JdbcDaoImpl.DEF_USERS_BY_USERNAME_QUERY);
        jdbcDao.setAuthoritiesByUsernameQuery(JdbcDaoImpl.DEF_AUTHORITIES_BY_USERNAME_QUERY);

        //        CachingUserDetailsService cachingUserDetailsService = new CachingUserDetailsService(jdbcDao);
        //        cachingUserDetailsService.setUserCache(userCache);

        // UserDetails kopieren, da bei ProviderManager.setEraseCredentialsAfterAuthentication(true)
        // das Password auf null gesetzt wird, kein zweiter Login mehr möglich, es folgt NullPointer.
        final UserDetailsService cachingUserDetailsService = username -> {
            UserDetails userDetails = userCache.getUserFromCache(username);

            if (userDetails == null) {
                userDetails = jdbcDao.loadUserByUsername(username);
            }

            Assert.notNull(userDetails, () -> "UserDetailsService " + jdbcDao + " returned null for username " + username + ". This is an interface contract violation");

            userCache.putUserInCache(userDetails);

            // return User.withUserDetails(userDetails).build();

            // @formatter:off
            return new User(
                    userDetails.getUsername()
                    , userDetails.getPassword()
                    , userDetails.isEnabled()
                    , userDetails.isAccountNonExpired()
                    , userDetails.isCredentialsNonExpired()
                    , userDetails.isAccountNonLocked()
                    , userDetails.getAuthorities()
            );
            // @formatter:on
        };

        return cachingUserDetailsService;

        // final JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);
        // userDetailsManager.setUserCache(userCache);
        // userDetailsManager.setUsersByUsernameQuery(JdbcDaoImpl.DEF_USERS_BY_USERNAME_QUERY);
        // userDetailsManager.setAuthoritiesByUsernameQuery(JdbcDaoImpl.DEF_AUTHORITIES_BY_USERNAME_QUERY);
        //
        // return userDetailsManager;
    }
}
