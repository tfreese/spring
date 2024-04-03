package de.freese.spring.openapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Thomas Freese
 */
@Configuration
//@OpenAPIDefinition(
//        info = @Info(
//                title = "User API",
//                version = "0.0.1-SNAPSHOT",
//                contact = @Contact(
//                        name = "Baeldung", email = "user-apis@baeldung.com", url = "https://www.baeldung.com"
//                ),
//                license = @License(
//                        name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0"
//                ),
//                termsOfService = "TODO",
//                description = "My Project"
//        ),
//        servers = @Server(
//                url = "My Server Url",
//                description = "Production"
//        )
//)
public class OpenApiConfiguration {
    @Bean
    public OpenAPI customOpenAPI() {
        final String description = """
                This is a sample OpenApi service.
                """;

        return new OpenAPI()
                .info(new Info().title("OpenApi Demo")
                        .version("0.0.1-SNAPSHOT")
                        .description(description)
                        .contact(new Contact().email("my@mail.de"))
                        .termsOfService("http://swagger.io/terms/")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org"))
                )
                ;
    }

    // @Bean
    // SecurityFilterChain filterChain(final HttpSecurity httpSecurity, final Filter jwtRequestFilter, final AuthenticationEntryPoint authenticationEntryPoint)
    //         throws Exception {
    //     httpSecurity
    //             .antMatchers("/api-docs", "/swagger-ui.html", "/webjars/**", "/api-docs", "/swagger-resources/**").permitAll()
    //             .anyRequest().authenticated()
    //     ;
    //
    //     return httpSecurity.build();
    // }
}
