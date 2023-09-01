// Created: 11.12.2021
package de.freese.spring.jwt;

import java.io.IOException;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.DefaultResponseErrorHandler;

import de.freese.spring.jwt.token.JwtTokenProvider;

/**
 * @author Thomas Freese
 */
@AutoConfigureMockMvc
abstract class AbstractTestJwtToken implements TestJwtToken {
    /**
     * Das Auslesen des Responses ist nur einmal möglich !<br>
     * Das führt bei den Tests zu Fehlern, da für die Asserts ein 2. x auf dem Response zugegriffen werden muss.
     *
     * @author Thomas Freese
     */
    private static final class NoOpResponseErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(final ClientHttpResponse response) throws IOException {
            // Das Auslesen des Responses ist nur einmal möglich !
            // Das führt bei den Tests zu Fehlern, da für die Asserts ein 2. x auf dem Response zugegriffen werden muss.

            // RestClientResponseException exception =
            // new RestClientResponseException("Server Error: [" + response.getRawStatusCode() + "]" + " " + response.getStatusText(),
            // response.getRawStatusCode(), response.getStatusText(), response.getHeaders(), getResponseBody(response), getCharset(response));
            //
            // System.err.println(exception);

            // try
            // {
            // ApiError apiError = TestRestApi.this.objectMapper.readValue(exception.getResponseBodyAsByteArray(), ApiError.class);
            // // exception.setStackTrace(apiError.getStackTrace());
            // System.err.println(apiError);
            // }
            // catch (Exception ex)
            // {
            // // Empty
            // }
        }
    }

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Resource
    private MockMvc mockMvc;

    @Resource
    private RestTemplateBuilder restTemplateBuilder;

    @Override
    public JwtTokenProvider getJwtTokenProvider() {
        return this.jwtTokenProvider;
    }

    @Override
    public MockMvc getMockMvc() {
        return this.mockMvc;
    }

    @Override
    public RestTemplateBuilder getRestTemplateBuilder() {
        return this.restTemplateBuilder;
    }

    @BeforeEach
    void beforeEach(@LocalServerPort final int localServerPort) throws Exception {
        String rootUri = "http://localhost:" + localServerPort;

        // @formatter:off
       this.restTemplateBuilder = this.restTemplateBuilder
               .rootUri(rootUri)
               .errorHandler(new NoOpResponseErrorHandler())
               ;
       // @formatter:on
    }
}
