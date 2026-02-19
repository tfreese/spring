package de.freese.spring.kryo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import de.freese.spring.kryo.web.KryoHttpMessageConverter;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public class KryoApplication implements WebMvcConfigurer {

    static void main(final String[] args) {
        SpringApplication.run(KryoApplication.class, args);
        // new SpringApplicationBuilder(KryoApplication.class).run(args);
    }

    @Override
    public void configureMessageConverters(final HttpMessageConverters.ServerBuilder builder) {
        builder.addCustomConverter(new KryoHttpMessageConverter(kryoPool()));
    }

    @Bean
    public KryoPool kryoPool() {
        return new KryoPool();
    }
}
