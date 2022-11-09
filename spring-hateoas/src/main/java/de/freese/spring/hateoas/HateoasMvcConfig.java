// Created: 01.12.2021
package de.freese.spring.hateoas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsConfiguration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Thomas Freese
 */
@Configuration
public class HateoasMvcConfig implements WebMvcConfigurer
{
    /**
     * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#extendMessageConverters(java.util.List)
     */
    @Override
    public void extendMessageConverters(final List<HttpMessageConverter<?>> converters)
    {
        // @formatter:off
        Optional<MappingJackson2HttpMessageConverter> converterOptional = converters.stream()
                //.peek(c -> System.out.println(c.getClass().getSimpleName()))
                .filter(MappingJackson2HttpMessageConverter.class::isInstance)
                .map(MappingJackson2HttpMessageConverter.class::cast)
                .findFirst()
                ;
        // @formatter:on

        if (converterOptional.isPresent())
        {
            MappingJackson2HttpMessageConverter converter = converterOptional.get();

            // converter.setObjectMapper(objectMapper());
            converter.getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            converter.getObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        }
    }

    @Bean
    HalFormsConfiguration halFormsConfiguration()
    {
        // @formatter:off
        return new HalFormsConfiguration()
                .withPattern(LocalDate.class, "yyyy-MM-dd")
                .withPattern(LocalDateTime.class, "yyyy-MM-dd HH:mm:ss")
                .withPattern(Date.class, "yyyy-MM-dd HH:mm:ss")
                ;
        // @formatter:on
    }

    // /**
    // * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#configureMessageConverters(java.util.List)
    // */
    // @Override
    // public void configureMessageConverters(final List<HttpMessageConverter<?>> converters)
    // {
    // for (HttpMessageConverter<?> httpMessageConverter : converters)
    // {
    // System.out.println(httpMessageConverter.getClass().getSimpleName());
    // }
    //
    // MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
    // jackson2HttpMessageConverter.setObjectMapper(objectMapper());
    // // jackson2HttpMessageConverter.setPrettyPrint(true);
    //
    // converters.add(jackson2HttpMessageConverter);
    // }

    // @Bean
    // public ObjectMapper objectMapper()
    // {
    // ObjectMapper mapper = new ObjectMapper();
    // mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    // // mapper.enable(SerializationFeature.INDENT_OUTPUT);
    //
    // return mapper;
    // }
}
