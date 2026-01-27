// Created: 27.01.2026
package de.freese.spring.data.jpa.config;

/**
 * @author Thomas Freese
 */
public class SecurityConfig {
    // /**
    //  * For H2-Console.<br>
    //  * Need org.springframework.boot:spring-boot-starter-security.
    //  */
    // @Bean
    // SecurityFilterChain configure(final HttpSecurity http) {
    //     return http
    //             // .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
    //             // .headers(AbstractHttpConfigurer::disable)
    //             .csrf(AbstractHttpConfigurer::disable)
    //             .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
    //             .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
    //
    //             // .authorizeHttpRequests(auth -> auth
    //             //         .requestMatchers("/h2-console/**").permitAll()
    //             //         .anyRequest().permitAll()
    //             // )
    //
    //             .build();
    // }
}
