// Created: 17.07.2026
package de.spring.ai.simple;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public class GithubCopilotApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(GithubCopilotApplication.class);

    static void main(final String[] args) {
        SpringApplication.run(GithubCopilotApplication.class, args);
    }

    public GithubCopilotApplication() {
        super();
    }

    @Bean
    ChatMemoryRepository chatMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }

    @Bean
    RestClientCustomizer restClientCustomizer() {
        final HttpClient httpClient = HttpClient.newBuilder()
                .build();

        return restClientBuilder ->
                restClientBuilder
                        // Required to read the Response multiple times (Spring > 7.0).
                        .bufferContent((uri, method) -> true)
                        .requestFactory(new JdkClientHttpRequestFactory(httpClient))

                        // Required to read the Response multiple times (Spring < 7.0).
                        // .requestFactory(new BufferingClientHttpRequestFactory(new JdkClientHttpRequestFactory(httpClient)))

                        .requestInterceptor((request, body, execution) -> {
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("Request: {} / {} / {} / {}",
                                                request.getMethod(),
                                                request.getURI(),
                                                request.getHeaders(),
                                                new String(body, StandardCharsets.UTF_8));
                                    }

                                    final ClientHttpResponse response = execution.execute(request, body);

                                    MDC.put("x-litellm-key-max-budget", response.getHeaders().getFirst("x-litellm-key-max-budget"));
                                    MDC.put("x-litellm-key-spend", response.getHeaders().getFirst("x-litellm-key-spend"));
                                    MDC.put("x-litellm-response-cost", response.getHeaders().getFirst("x-litellm-response-cost"));

                                    if (LOGGER.isDebugEnabled()) {
                                        String bodyText = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

                                        if (bodyText.length() > 8192) {
                                            bodyText = bodyText.substring(0, 8192) + "...";
                                        }

                                        LOGGER.debug("Response: {} / {} / {}",
                                                response.getStatusCode(),
                                                response.getHeaders(),
                                                bodyText);
                                    }

                                    return response;
                                }
                        );
    }
}
