// Erzeugt: 04.05.2016
package de.freese.spring.hateoas;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
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
     * Konfiguriert die SpringApplication.
     *
     * @param builder {@link SpringApplicationBuilder}
     * @return {@link SpringApplicationBuilder}
     */
    private static SpringApplicationBuilder configureApplication(final SpringApplicationBuilder builder)
    {
        // headless(false) für Desktop
        return builder.sources(HateoasApplication.class).headless(true);// .bannerMode(Banner.Mode.OFF);
    }

    /**
     * @param args String[]
     * @throws java.lang.Exception Falls was schief geht.
     */
    @SuppressWarnings("resource")
    public static void main(final String[] args) throws Exception
    {
        ApplicationContext context = configureApplication(new SpringApplicationBuilder()).run(args);

        int port = context.getEnvironment().getProperty("local.server.port", Integer.class);
        Optional<String> contextPath = Optional.ofNullable(context.getEnvironment().getProperty("server.servlet.context-path", String.class));

        URI uri = URI.create("http://localhost:" + port + contextPath.orElse("") + "/greeter");

        try
        {
            // Firefox: view-source:URI
            Runtime.getRuntime().exec("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe " + uri);
        }
        catch (Exception ex)
        {
            try
            {
                // Linux
                Runtime.getRuntime().exec("firefox " + uri);
            }
            catch (Exception ex2)
            {
                // IE
                Desktop.getDesktop().browse(uri);
            }
        }
    }

    /**
     * Erzeugt eine neue Instanz von {@link HateoasApplication}
     */
    public HateoasApplication()
    {
        super();
    }

    /**
     * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#extendMessageConverters(java.util.List)
     */
    @Override
    public void extendMessageConverters(final List<HttpMessageConverter<?>> converters)
    {
        Optional<MappingJackson2HttpMessageConverter> converter = converters.stream().peek(c -> System.out.println(c.getClass().getSimpleName()))
                .filter(c -> c instanceof MappingJackson2HttpMessageConverter).map(c -> (MappingJackson2HttpMessageConverter) c).findFirst();

        if (converter.isPresent())
        {
            // converter.get().setObjectMapper(objectMapper());
            converter.get().getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            converter.get().getObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        }
    }
    // /**
    // * @see
    // *
    // org.springframework.boot.web.support.SpringBootServletInitializer#configure(org.springframework.boot.builder.SpringApplicationBuilder)
    // */
    // @Override
    // protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder)
    // {
    // return configureApplication(builder);
    // }
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
