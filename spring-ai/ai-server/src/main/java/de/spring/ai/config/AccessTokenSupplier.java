package de.spring.ai.config;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.retry.RetryListener;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryState;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.core.retry.Retryable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The Access-Token is valid for 30 minutes.<br>
 * This class is scheduled to retrieve it every 25 minutes.
 *
 * @author Thomas Freese (D99QAIA)
 */
public final class AccessTokenSupplier implements Supplier<String>, Runnable {
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenSupplier.class);
    private final String clientId;
    private final String clientSecret;
    private final JsonMapper jsonMapper;
    private final RestClient restClient;
    private final RetryTemplate retryTemplate;

    private String accessToken;

    public AccessTokenSupplier(final String clientId,
                               final String clientSecret,
                               final URI uri,
                               final JsonMapper jsonMapper) {
        super();

        Objects.requireNonNull(clientId, "clientId required");
        Objects.requireNonNull(clientSecret, "clientSecret required");
        Objects.requireNonNull(uri, "uri required");
        Objects.requireNonNull(jsonMapper, "jsonMapper required");

        final HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();

        final HttpClient httpClient = httpClientBuilder.build();

        restClient = RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .baseUrl(uri)
                .build();

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.jsonMapper = jsonMapper;

        LOGGER.debug("Using CLIENT_ID='{}'; CLIENT_SECRET='{}'; URI={}", clientId, clientSecret, uri);

        retryTemplate = new RetryTemplate(RetryPolicy.builder()
                .maxRetries(2)
                .delay(Duration.ofSeconds(1))
                .build());
        retryTemplate.setRetryListener(new RetryListener() {
            @Override
            public void beforeRetry(final RetryPolicy retryPolicy, final Retryable<?> retryable) {
                LOGGER.warn("Retry: {}", retryable.getName());
            }

            @Override
            public void onRetryableExecution(final RetryPolicy retryPolicy, final Retryable<?> retryable, final RetryState retryState) {
                if (!retryState.isSuccessful()) {
                    LOGGER.warn("Will Retry: {} - {}", retryable.getName(), retryState.getRetryCount());
                }
            }
        });
    }

    @Override
    public String get() {
        return accessToken;
    }

    /**
     * The token is valid for 30 minutes, to be on the safe side refresh it every 25 Minutes.
     */
    @Scheduled(timeUnit = TimeUnit.SECONDS, initialDelay = 1L, fixedDelay = 25L * 60L)
    @Override
    public void run() {
        try {
            accessToken = retryTemplate.execute(this::retrieveNewToken);
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    // org.springframework.retry.annotation.EnableRetry
    // org.springframework.retry.annotation.EnableResilientMethods
    // org.springframework.retry.annotation@Retryable(maxRetries = 2, delay = 1, timeUnit = TimeUnit.SECONDS)
    private String retrieveNewToken() {
        LOGGER.info("Retrieve new Access-Token...");

        if (clientId.isBlank() || clientSecret.isBlank()) {
            LOGGER.warn("Missing clientId or clientSecret");

            return null;
        }

        final MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        final ResponseEntity<String> responseEntity = restClient.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .toEntity(String.class);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Response: {}", responseEntity.getBody());
        }

        final JsonNode jsonNode = jsonMapper.readTree(responseEntity.getBody());

        final String token = jsonNode.get("access_token").asString();
        final int expiresIn = jsonNode.get("expires_in").asInt();

        if (expiresIn > 0 && LOGGER.isInfoEnabled()) {
            final LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresIn);
            LOGGER.info("... retrieved new Access-Token, expires at {}", DATETIME_FORMATTER.format(expiresAt));
        }

        return token;
    }
}
