// Created: 11.03.2020
package de.freese.spring.rsocket;

import java.time.Duration;
import java.time.LocalDateTime;

import javax.annotation.PreDestroy;

import de.freese.spring.rsocket.model.MessageRequest;
import de.freese.spring.rsocket.model.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@Controller
public class RSocketController
{
    /**
    *
    */
    private static final Logger LOGGER = LoggerFactory.getLogger(RSocketController.class);

    /**
     * @param requests {@link Flux}
     * @param user {@link UserDetails}
     *
     * @return {@link Flux}
     */
    @PreAuthorize("hasRole('USER')")
    @MessageMapping("channel")
    Flux<MessageResponse> channel(final Flux<MessageRequest> requests, @AuthenticationPrincipal final UserDetails user)
    {
        // ReactiveSecurityContextHolder.getContext().map(SecurityContext::getAuthentication).subscribe(authentication -> {
        // LOGGER.info("{}", authentication);
        // LOGGER.info("Channel initiated by '{}' in the role '{}'", authentication.getPrincipal(), authentication.getAuthorities());
        // });

        LOGGER.info("Received channel request (stream) at {}", LocalDateTime.now());
        LOGGER.info("Channel initiated by '{}' in the role '{}'", user.getUsername(), user.getAuthorities());

        // @formatter:off
        return requests
                // Indizierung
                .index()
                // Flux-Events der Requests loggen.
                .log()
                // Pro Element 1 Sekunde warten.
                .delayElements(Duration.ofSeconds(1))
                // Response-Objekt erzeugen.
                .map(objects -> new MessageResponse( objects.getT2().getMessage() , objects.getT1()))
                // Flux-Events der Responses loggen.
                .log()
                ;
        // @formatter:on
    }

    /**
     * @return {@link MessageResponse}
     */
    @MessageMapping("error")
    Mono<MessageResponse> error()
    {
        return Mono.error(new IllegalArgumentException("Bad Exception"));
    }

    /**
     * @param th {@link Throwable}
     *
     * @return {@link Mono}
     */
    @MessageExceptionHandler
    Mono<MessageResponse> errorHandler(final Throwable th)
    {
        MessageResponse response = new MessageResponse();
        response.setMessage(th.getClass().getSimpleName() + ": " + th.getMessage());

        return Mono.just(response);
    }

    /**
     * @param request {@link MessageRequest}
     * @param user {@link UserDetails}
     *
     * @return {@link Mono}
     */
    @PreAuthorize("hasRole('USER')")
    @MessageMapping("fire-and-forget")
    Mono<Void> fireAndForget(final MessageRequest request, @AuthenticationPrincipal final UserDetails user)
    {
        LOGGER.info("Received fire-and-forget request: {}", request);
        LOGGER.info("Fire-And-Forget initiated by '{}' in the role '{}'", user.getUsername(), user.getAuthorities());

        return Mono.empty();
    }

    /**
     * @param name String
     *
     * @return {@link Mono}
     */
    @PreAuthorize("hasRole('USER')")
    @MessageMapping("parameter/{name}")
    Mono<MessageResponse> parameter(@DestinationVariable final String name)
    {
        LOGGER.info("Received parameter request: {}", name);

        return Mono.just(new MessageResponse(name));
    }

    /**
     * @param request {@link MessageRequest}
     * @param user {@link UserDetails}
     *
     * @return {@link Mono}
     */
    @PreAuthorize("hasRole('USER')")
    @MessageMapping("request-response")
    Mono<MessageResponse> requestResponse(final MessageRequest request, @AuthenticationPrincipal final UserDetails user)
    {
        LOGGER.info("Received request-response request: {}", request);
        LOGGER.info("Request-response initiated by '{}' in the role '{}'", user.getUsername(), user.getAuthorities());

        return Mono.just(new MessageResponse(request.getMessage()));
    }

    /**
     *
     */
    @PreDestroy
    void shutdown()
    {
        LOGGER.info("Shutting down.");
    }

    /**
     * @param request {@link MessageRequest}
     * @param user {@link UserDetails}
     *
     * @return {@link Flux}
     */
    @PreAuthorize("hasRole('USER')")
    @MessageMapping("stream")
    Flux<MessageResponse> stream(final MessageRequest request, @AuthenticationPrincipal final UserDetails user)
    {
        LOGGER.info("Received stream request: {}", request);
        LOGGER.info("Stream initiated by '{}' in the role '{}'", user.getUsername(), user.getAuthorities());

        // @formatter:off
        return Flux
                // Jede Sekunde ein neues Element erzeugen.
                .interval(Duration.ofSeconds(1))
                // Nur die ersten 3 Elemente nehmen.
                .take(3L)
                // Indizierung
                .index()
                // Response-Objekt erzeugen.
                .map(objects -> new MessageResponse( request.getMessage(), objects.getT1()))
                // Flux-Events loggen.
                .log()
                ;
        // @formatter:on
    }
}
