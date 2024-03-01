package de.freese.spring.jwt.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final RsaKeyProperties rsaKeyProperties;

    public SecurityConfig(final RsaKeyProperties rsaKeyProperties) {
        super();

        this.rsaKeyProperties = Objects.requireNonNull(rsaKeyProperties, "rsaKeyProperties required");
    }

    /**
     * Prevent "SCOPE_" Prefix for Authorities.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        final JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        final JwtAuthenticationConverter authConverter = new JwtAuthenticationConverter();
        authConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return authConverter;
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://localhost:8080"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedMethods(List.of("GET"));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(rsaKeyProperties.publicKey()).build();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        final JWK jwk = new RSAKey.Builder(rsaKeyProperties.publicKey()).privateKey(rsaKeyProperties.privateKey()).build();
        final JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));

        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        final Pbkdf2PasswordEncoder pbkdf2passwordEncoder =
                new Pbkdf2PasswordEncoder("mySecret", 16, 310_000, Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA512);
        pbkdf2passwordEncoder.setEncodeHashAsBase64(false);

        final Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("pbkdf2", pbkdf2passwordEncoder);
        encoders.put("bcrypt", new BCryptPasswordEncoder(10));
        // encoders.put("scrypt", new SCryptPasswordEncoder()); // Benötigt BountyCastle
        // encoders.put("argon2", new Argon2PasswordEncoder()); // Benötigt BountyCastle
        encoders.put("noop", new PasswordEncoder() {
            @Override
            public String encode(final CharSequence rawPassword) {
                return rawPassword.toString().replace("{noop}", "");
            }

            @Override
            public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }
        });

        return new DelegatingPasswordEncoder("noop", encoders);
    }

    @Bean
    SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        // @formatter:off
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .oauth2ResourceServer(oAuth2ResourceServerConfigurer -> oAuth2ResourceServerConfigurer.jwt(Customizer.withDefaults()))
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                                .accessDeniedHandler(new BearerTokenAccessDeniedHandler()))
                // .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
        // @formatter:on
    }

    /*
     * This will allow the /token endpoint to use basic auth and everything else uses the SFC above.
     */
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Bean
    SecurityFilterChain securityFilterChainToken(final HttpSecurity http) throws Exception {
        // @formatter:off
        return http
                .securityMatcher(new AntPathRequestMatcher("/token"))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex ->
                    ex.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                            .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
                ).httpBasic(Customizer.withDefaults())
                .build();
        // @formatter:on
    }

    @Bean
    @ConditionalOnBean(DataSource.class)
    UserDetailsService userDetailsServiceJdbc(final PasswordEncoder passwordEncoder, final DataSource dataSource, final UserCache userCache) {
        final JdbcDaoImpl jdbcDao = new JdbcDaoImpl();
        jdbcDao.setDataSource(dataSource);
        jdbcDao.setUsersByUsernameQuery(JdbcDaoImpl.DEF_USERS_BY_USERNAME_QUERY);
        jdbcDao.setAuthoritiesByUsernameQuery(JdbcDaoImpl.DEF_AUTHORITIES_BY_USERNAME_QUERY);

        // final CachingUserDetailsService cachingUserDetailsService = new CachingUserDetailsService(jdbcDao);
        //        cachingUserDetailsService.setUserCache(userCache);

        // UserDetails kopieren, da bei ProviderManager.setEraseCredentialsAfterAuthentication(true)
        // das Password auf null gesetzt wird, kein zweiter Login mehr möglich, es folgt NullPointer.
        return username -> {
            UserDetails userDetails = userCache.getUserFromCache(username);

            if (userDetails == null) {
                userDetails = jdbcDao.loadUserByUsername(username);
            }

            Assert.notNull(userDetails, () -> "UserDetailsService " + jdbcDao + " returned null for username " + username + ". This is an interface contract violation");

            userCache.putUserInCache(userDetails);

            return User.withUserDetails(userDetails).build();

            // // @formatter:off
            // return new User(
            //         userDetails.getUsername()
            //         , userDetails.getPassword()
            //         , userDetails.isEnabled()
            //         , userDetails.isAccountNonExpired()
            //         , userDetails.isCredentialsNonExpired()
            //         , userDetails.isAccountNonLocked()
            //         , userDetails.getAuthorities()
            // );
            // // @formatter:on
        };

        // final JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);
        // userDetailsManager.setUserCache(userCache);
        // userDetailsManager.setUsersByUsernameQuery(JdbcDaoImpl.DEF_USERS_BY_USERNAME_QUERY);
        // userDetailsManager.setAuthoritiesByUsernameQuery(JdbcDaoImpl.DEF_AUTHORITIES_BY_USERNAME_QUERY);
        //
        // return userDetailsManager;
    }

    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    UserDetailsService userDetailsServiceMemory(final PasswordEncoder passwordEncoder) {
        final InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();

        userDetailsManager.createUser(
                User.withUsername("admin").password(passwordEncoder.encode("{noop}adminpw")).passwordEncoder(passwordEncoder::encode).authorities("ADMIN", "USER").build());
        userDetailsManager.createUser(
                User.withUsername("user").password(passwordEncoder.encode("{noop}userpw")).passwordEncoder(passwordEncoder::encode).authorities("USER").build());

        // roles(...) This method is a shortcut for calling authorities(String...), but automatically prefixes each entry with "ROLE_".
        // userDetailsManager.createUser(
        //      User.withUsername("admin").password(passwordEncoder.encode("{noop}adminpw")).passwordEncoder(passwordEncoder::encode).roles("ADMIN", "USER").build());
        // userDetailsManager.createUser(
        //      User.withUsername("user").password(passwordEncoder.encode("{noop}userpw")).passwordEncoder(passwordEncoder::encode).roles("USER").build());

        return userDetailsManager;
    }
}
