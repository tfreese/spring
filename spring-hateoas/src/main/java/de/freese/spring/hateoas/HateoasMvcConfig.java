// Created: 01.12.2021
package de.freese.spring.hateoas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsConfiguration;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Thomas Freese
 */
@Configuration
public class HateoasMvcConfig implements WebMvcConfigurer {
    @Override
    public void configureMessageConverters(final HttpMessageConverters.ServerBuilder builder) {
        final JsonMapper jsonMapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        final JacksonJsonHttpMessageConverter jsonHttpMessageConverter = new JacksonJsonHttpMessageConverter(jsonMapper);

        builder.addCustomConverter(jsonHttpMessageConverter);
    }

    @Bean
    HalFormsConfiguration halFormsConfiguration() {
        return new HalFormsConfiguration()
                .withPattern(LocalDate.class, "yyyy-MM-dd")
                .withPattern(LocalDateTime.class, "yyyy-MM-dd HH:mm:ss")
                .withPattern(Date.class, "yyyy-MM-dd HH:mm:ss")
                ;
    }

    // @Override
    // public void configureMessageConverters(final List<HttpMessageConverter<?>> converters)  {
    // for (HttpMessageConverter<?> httpMessageConverter : converters) {
    // System.out.println(httpMessageConverter.getClass().getSimpleName());
    // }
    //
    // final MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
    // jackson2HttpMessageConverter.setObjectMapper(objectMapper());
    // // jackson2HttpMessageConverter.setPrettyPrint(true);
    //
    // converters.add(jackson2HttpMessageConverter);
    // }

    // @Bean
    // public ObjectMapper objectMapper() {
    // final ObjectMapper mapper = new ObjectMapper();
    // mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    // // mapper.enable(SerializationFeature.INDENT_OUTPUT);
    //
    // return mapper;
    // }
}
