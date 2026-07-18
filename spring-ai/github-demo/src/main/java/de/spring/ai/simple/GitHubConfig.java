package de.spring.ai.simple;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Thomas Freese
 * @since 18.07.26
 */
@Configuration
public class GitHubConfig {
    @Bean
    public ChatClient gitHubChatClient(@Value("${spring.ai.openai.base-url}") final String baseUrl,
                                       @Value("${spring.ai.openai.api-key}") final String apiKey,
                                       @Value("${spring.ai.openai.chat.model}") final String model
    ) {

        // Wir umgehen das Autowiring und setzen die nackte Ziel-URL direkt in die API-Klasse.
        final OpenAiChatOptions options = OpenAiChatOptions.builder()
                .baseUrl(baseUrl) // spring-ai hängt hier automatisch ein '/v1' an die base-url an.
                .apiKey(apiKey)
                .model(model)
                .customHeaders(
                        // Hier zwingend die von GitHub verlangten Header injizieren.
                        Map.of(
                                "X-GitHub-Api-Version", "2026-03-10",
                                "Accept", "application/vnd.github+json"
                        ))
                .build();

        final OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .options(options)
                .build();

        return ChatClient.builder(chatModel)
                .defaultSystem("Du bist ein hilfreicher Assistent auf GitHub.")
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    // @Bean
    // ChatMemoryRepository chatMemoryRepository() {
    //     return new InMemoryChatMemoryRepository();
    // }
    //
    // @Bean
    // RestClientCustomizer restClientCustomizer() {
    //     final HttpClient httpClient = HttpClient.newBuilder()
    //             .build();
    //
    //     return restClientBuilder ->
    //             restClientBuilder
    //                     // Required to read the Response multiple times (Spring > 7.0).
    //                     .bufferContent((uri, method) -> true)
    //                     .requestFactory(new JdkClientHttpRequestFactory(httpClient))
    //
    //                     // Required to read the Response multiple times (Spring < 7.0).
    //                     // .requestFactory(new BufferingClientHttpRequestFactory(new JdkClientHttpRequestFactory(httpClient)))
    //
    //                     .requestInterceptor((request, body, execution) -> {
    //                                 if (LOGGER.isDebugEnabled()) {
    //                                     LOGGER.debug("Request: {} / {} / {} / {}",
    //                                             request.getMethod(),
    //                                             request.getURI(),
    //                                             request.getHeaders(),
    //                                             new String(body, StandardCharsets.UTF_8));
    //                                 }
    //
    //                                 final ClientHttpResponse response = execution.execute(request, body);
    //
    //                                 MDC.put("x-litellm-key-max-budget", response.getHeaders().getFirst("x-litellm-key-max-budget"));
    //                                 MDC.put("x-litellm-key-spend", response.getHeaders().getFirst("x-litellm-key-spend"));
    //                                 MDC.put("x-litellm-response-cost", response.getHeaders().getFirst("x-litellm-response-cost"));
    //
    //                                 if (LOGGER.isDebugEnabled()) {
    //                                     String bodyText = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
    //
    //                                     if (bodyText.length() > 8192) {
    //                                         bodyText = bodyText.substring(0, 8192) + "...";
    //                                     }
    //
    //                                     LOGGER.debug("Response: {} / {} / {}",
    //                                             response.getStatusCode(),
    //                                             response.getHeaders(),
    //                                             bodyText);
    //                                 }
    //
    //                                 return response;
    //                             }
    //                     );
    // }
}
