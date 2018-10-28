/**
 * Created: 28.10.2018
 */

package org.spring.oauth.jwt.config;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.common.base.Predicates;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig
{
    /**
     * @return {@link Docket}
     */
    @Bean
    public Docket api()
    {
        // @formatter:off
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(Predicates.not(PathSelectors.regex("/error")))
                .build()
                .apiInfo(metadata())
                .useDefaultResponseMessages(false)
                .securitySchemes(Arrays.asList(new ApiKey("Bearer %token", "Authorization", "Header")))
                .tags(new Tag("users", "Operations about users")) // Verknüpfung mit UserController.
//                .tags(new Tag("ping", "Just a ping"))
                .genericModelSubstitutes(Optional.class);
        // @formatter:on
    }

    /**
     * @return {@link ApiInfo}
     */
    private ApiInfo metadata()
    {
        // @formatter:off
        return new ApiInfoBuilder()
                .title("JSON Web Token Authentication API")
                .description(
                        "This is a sample JWT authentication service. You can find out more about JWT at [https://jwt.io/](https://jwt.io/). For this sample, you can use the `admin` or `user` users (password for booth: pw) to test the authorization filters. Once you have successfully logged in and obtained the token, you should click on the right top button `Authorize` and introduce it with the prefix \"Bearer \".")
                .version("1.0.0")
                .license("MIT License").licenseUrl("http://opensource.org/licenses/MIT")
                .contact(new Contact(null, null, "commercial@freese-home.de"))
                .build();
        // @formatter:on
    }
}