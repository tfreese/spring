package de.spring.react.config;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * @author Thomas Freese
 * @since 17.10.26
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /**
     * Konfiguriert die Cross-Origin-Resource-Sharing-Regeln für Browser-Clients.
     * <p>
     * Diese Bean ist nur dann relevant, wenn Frontend und Backend auf unterschiedlichen Origins laufen,
     * z. B. Frontend auf {@code http://localhost:5173} und Backend auf {@code http://localhost:8080}.
     * Ohne diese Konfiguration würde der Browser Cross-Origin-Requests blockieren, obwohl der Server
     * technisch erreichbar wäre.
     * </p>
     * Die Regeln erlauben hier nur lokale Entwicklungs-Origins, standardisierte HTTP-Methoden,
     * beliebige Header und Credentials, damit die React-App mit dem Spring-Boot-Backend auf localhost
     * kommunizieren kann. In einer echten Produktivumgebung sollten die Origins explizit auf die
     * tatsächlich erlaubten Domains eingeschränkt werden.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:[*]", "http://127.0.0.1:[*]"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    /**
     * Erlaubt den Zugriff auf /api nur von localhost.<br/>
     * Der Zugriff auf /public ist nicht eingeschränkt.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/api/**").access((authentication, context) -> {
                            final HttpServletRequest request = context.getRequest();
                            return new AuthorizationDecision(isLocalRequest(request));
                        })
                        // .requestMatchers("/api/**").access((authentication, context) -> {
                        //     final String remoteAddress = context.getRequest().getRemoteAddr();
                        //     final boolean isLocalhost = new IpAddressMatcher("127.0.0.1").matches(remoteAddress)
                        //             || new IpAddressMatcher("::1").matches(remoteAddress);
                        //     return new org.springframework.security.authorization.AuthorizationDecision(isLocalhost);
                        // })
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    private boolean isLocalRequest(final HttpServletRequest request) {
        if (isLoopback(request.getRemoteAddr()) || isLoopback(request.getServerName())) {
            return true;
        }

        final String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null) {
            for (final String value : forwardedFor.split(",")) {
                if (isLoopback(value.strip())) {
                    return true;
                }
            }
        }

        final String forwardedHost = request.getHeader("X-Forwarded-Host");

        if (forwardedHost != null) {
            for (final String value : forwardedHost.split(",")) {
                if (isLoopback(value.strip())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isLoopback(final String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String normalized = value.strip();

        if (normalized.startsWith("[") && normalized.contains("]")) {
            normalized = normalized.substring(1, normalized.indexOf(']'));
        }

        return "localhost".equalsIgnoreCase(normalized)
                || "127.0.0.1".equals(normalized)
                || "::1".equals(normalized)
                || "0:0:0:0".equals(normalized)
                || "[::1]".equalsIgnoreCase(normalized);
    }
}
