//// Created:s 28.10.2018
// package de.freese.spring.jwt.config;
//
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Profile;
// import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
// import springfox.documentation.builders.ApiInfoBuilder;
// import springfox.documentation.builders.PathSelectors;
// import springfox.documentation.builders.RequestHandlerSelectors;
// import springfox.documentation.service.ApiInfo;
// import springfox.documentation.service.Contact;
// import springfox.documentation.spi.DocumentationType;
// import springfox.documentation.spring.web.plugins.Docket;
// import springfox.documentation.swagger2.annotations.EnableSwagger2;
//
/// **
// * @author Thomas Freese
// */
// @Configuration
// @EnableSwagger2
// @Profile("!test")
// public class SwaggerConfig implements WebMvcConfigurer
// {
// /**
// * @see
//// org.springframework.web.servlet.config.annotation.WebMvcConfigurer#addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry)
// */
// @Override
// public void addResourceHandlers(final ResourceHandlerRegistry registry)
// {
// registry.addResourceHandler("/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
// registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
// registry.addResourceHandler("/static/**").addResourceLocations("/static/");
// }
//
// /**
// * Methodenname = ContextPath<br>
// * swagger-ui.html<br>
// * /api/swagger-resources/**<br>
// * /v2/api-docs<br>
// *
// * @return {@link Docket}
// */
// @Bean
// public Docket api()
// {
//        // @formatter:off
//        return new Docket(DocumentationType.SWAGGER_2)
//                .select()
//                .apis(RequestHandlerSelectors.basePackage("de.freese.spring.jwt"))
//                //.apis(RequestHandlerSelectors.any())
//                //.paths(Predicates.not(PathSelectors.regex("/error")))
//                .paths(PathSelectors.any())
//                .build()
//                //.pathMapping("/swagger")
//                .apiInfo(apiInfo())
//                //.useDefaultResponseMessages(false)
//                //.securitySchemes(Arrays.asList(new ApiKey("Bearer %token", "Authorization", "Header")))
//                //.tags(new Tag("users", "Operations about users"))
//                //.tags(new Tag("ping", "Just a ping"))
//                //.genericModelSubstitutes(Optional.class)
//                ;
//        // @formatter:on
// }
//
// /**
// * @return {@link ApiInfo}
// */
// private ApiInfo apiInfo()
// {
// String descripton =
// """
// This is a sample JWT authentication service.
// You can find out more about JWT at [https://jwt.io/](https://jwt.io/).
// For this sample, you can use the <b>admin</b> or <b>user</b> users (password for booth: <b>pass</b>) to test the authorization filters.
// Once you have successfully logged in and obtained the token, you should click on the right top button 'Authorize' and introduce it with the prefix "Bearer".
// """;
//
// System.out.println(descripton);
//
//        // @formatter:off
//        return new ApiInfoBuilder()
//                .title("JSON Web Token Authentication API")
//                .description(descripton)
//                .version("0.0.1-SNAPSHOT")
//                .license("MIT License").licenseUrl("http://opensource.org/licenses/MIT")
//                .contact(new Contact(null, null, "commercial@freese-home.de"))
//                .build()
//                ;
//        // @formatter:on
// }
// }
