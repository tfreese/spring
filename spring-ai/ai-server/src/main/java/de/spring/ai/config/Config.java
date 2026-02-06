package de.spring.ai.config;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Thomas Freese
 */
@Configuration
public class Config {
    /**
     * topK - Use first n documents from search result
     */
    public static final int RAG_MAX_SIMILARITY_RESULTS = 5;
    /**
     * Chat memory configuration
     */
    public static final int MEMORY_MAX_MESSAGES = 10;
    /**
     * RAG configuration (0-1)
     */
    public static final double RAG_MAX_THRESHOLD = 0.5D;

    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    @Bean
    AccessTokenSupplier accessTokenSupplier(@Value("${CLIENT_ID:none}") final String clientId,
                                                @Value("${CLIENT_SECRET:none}") final String clientSecret,
                                                @Value("${CLIENT_URL:none}") final URI uri,
                                                final JsonMapper jsonMapper) {
        return new AccessTokenSupplier(clientId, clientSecret, uri, jsonMapper);
    }

    // /**
    //  * See application.yml.
    //  */
    // @Bean
    // ChatOptions chatOptions() {
    //     // OpenAiChatOptions.builder()
    //     return ChatOptions.builder()
    //             .model("llama3.2")
    //             .temperature(0.5D)
    //             .topK(40)
    //             .topP(0.8D)
    //             .maxTokens(1024)
    //             .build();
    // }

    @Bean
    RestClientCustomizer restClientCustomizer(final AccessTokenSupplier accessTokenSupplier, @Value("${AI_API_KEY:none}") final String aiApiKey) {

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
                                    // Overwrite the existing Bearer, Spring AI set the API-Key already as Bearer, and two Bearer are wrong.
                                    request.getHeaders().setBearerAuth(accessTokenSupplier.get());

                                    request.getHeaders().add("X-LLM-API-CLIENT-ID", "Bearer " + aiApiKey);

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
