// Created: 04.05.2016
package de.freese.spring.hateoas;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * https://spring.io/guides/gs/rest-hateoas<br>
 * https://github.com/spring-guides/gs-rest-hateoas<br>
 * curl http://localhost:9000/greeter<br>
 * https://spring.io/guides/tutorials/bookmarks/
 *
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class HateoasApplication implements WebMvcConfigurer// extends SpringBootServletInitializer
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        SpringApplication.run(HateoasApplication.class, args);
    }

    /**
     * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#extendMessageConverters(java.util.List)
     */
    @Override
    public void extendMessageConverters(final List<HttpMessageConverter<?>> converters)
    {
        Optional<MappingJackson2HttpMessageConverter> converter = converters.stream().peek(c -> System.out.println(c.getClass().getSimpleName()))
                .filter(MappingJackson2HttpMessageConverter.class::isInstance).map(MappingJackson2HttpMessageConverter.class::cast).findFirst();

        if (converter.isPresent())
        {
            // converter.get().setObjectMapper(objectMapper());
            converter.get().getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            converter.get().getObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        }
    }

    // /**
    // * @see
    // * org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#configureMessageConverters(java.util.List)
    // */
    // @Override
    // public void configureMessageConverters(final List<HttpMessageConverter<?>> converters)
    // {
    // super.configureMessageConverters(converters);
    //
    // for (HttpMessageConverter<?> httpMessageConverter : converters)
    // {
    // System.out.println(httpMessageConverter.getClass().getSimpleName());
    // }
    //
    // MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
    // jackson2HttpMessageConverter.setObjectMapper(objectMapper());
    //// jackson2HttpMessageConverter.setPrettyPrint(true);
    //
    // converters.add(jackson2HttpMessageConverter);
    // }
    // /**
    // * @return {@link ObjectMapper}
    // */
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
