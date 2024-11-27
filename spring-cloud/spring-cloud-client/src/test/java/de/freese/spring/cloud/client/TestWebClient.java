// Created: 21.05.23
package de.freese.spring.cloud.client;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

/**
 * @author Thomas Freese
 */
class TestWebClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestWebClient.class);

    private static final MockWebServer SERVER = new MockWebServer();

    @AfterAll
    static void afterAll() throws IOException {
        SERVER.shutdown();
    }

    @BeforeAll
    static void beforeAll() throws IOException {
        SERVER.start();
        //        SERVER.start(InetAddress.getLoopbackAddress(), 11111);
    }

    ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            LOGGER.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            LOGGER.info("--- Http Headers: ---");
            clientRequest.headers().forEach(this::logValues);

            LOGGER.info("--- Http Cookies: ---");
            clientRequest.cookies().forEach(this::logValues);

            return next.exchange(clientRequest);
        };
    }

    ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            LOGGER.info("Response: {}", clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach(this::logValues);

            return Mono.just(clientResponse);
        });
    }

    @Test
    void testRetryForAllEndpoints() {
        SERVER.enqueue(new MockResponse().setResponseCode(500).setBody("{failure}"));
        SERVER.enqueue(new MockResponse().setResponseCode(500).setBody("{failure}"));
        SERVER.enqueue(new MockResponse().setResponseCode(500).setBody("{failure}"));
        SERVER.enqueue(new MockResponse().setResponseCode(200).setBody("{success}"));

        final ExchangeFilterFunction retryFilterFunction = (request, next) -> next.exchange(request)
                .flatMap(clientResponse -> Mono.just(clientResponse)
                        .filter(response -> clientResponse.statusCode().isError())
                        .flatMap(response -> clientResponse.createException())
                        .flatMap(Mono::error)
                        .thenReturn(clientResponse))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .doBeforeRetry(signal -> LOGGER.info("Retrying request: {}", signal))
                );

        final WebClient webClient = createWebClientBuilder().filter(retryFilterFunction).baseUrl(SERVER.url("/test").toString()).build();

        final Mono<String> responseMono1 = webClient
                .get()
                .uri("/api")
                .retrieve()
                .bodyToMono(String.class);

        StepVerifier.create(responseMono1).expectNextCount(1).verifyComplete();
    }

    @Test
    void testRetryForOneEndpoint() {
        SERVER.enqueue(new MockResponse().setResponseCode(500).setBody("{failure}"));
        SERVER.enqueue(new MockResponse().setResponseCode(500).setBody("{failure}"));
        SERVER.enqueue(new MockResponse().setResponseCode(500).setBody("{failure}"));
        SERVER.enqueue(new MockResponse().setResponseCode(200).setBody("{success}"));

        final WebClient webClient = createWebClientBuilder().baseUrl(SERVER.url("/test").toString()).build();

        final Mono<String> responseMono = webClient
                .get()
                .uri("/api")
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .doBeforeRetry(signal -> LOGGER.info("Retrying request: {}", signal))
                );

        StepVerifier.create(responseMono).expectNextCount(1).verifyComplete();
    }

    private WebClient.Builder createWebClientBuilder() {
        final HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
                .doOnConnected(connection ->
                        connection
                                .addHandlerLast(new ReadTimeoutHandler(2L, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(2L, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                //.filter(logRequest())
                //.filter(logResponse())
                ;
    }

    private void logValues(final String name, final List<String> values) {
        values.forEach(value -> LOGGER.info("{} = {}", name, value));
    }

    //    private LoadBalancedExchangeFilterFunction loadBalancerExchangeFilterFunction() {
    //        final LoadBalancerClientFactory loadBalancerClientFactory = new LoadBalancerClientFactory(new LoadBalancerClientsProperties());
    //
    //        return new ReactorLoadBalancerExchangeFilterFunction(loadBalancerClientFactory, null);
    //    }
}
