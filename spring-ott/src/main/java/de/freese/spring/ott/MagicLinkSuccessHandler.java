// Created: 05.04.2025
package de.freese.spring.ott;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.authentication.ott.RedirectOneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Thomas Freese
 */
@Component
public final class MagicLinkSuccessHandler implements OneTimeTokenGenerationSuccessHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MagicLinkSuccessHandler.class);

    private static final String REDIRECT_URL = "/ott/sent";

    private final OneTimeTokenGenerationSuccessHandler handler = new RedirectOneTimeTokenGenerationSuccessHandler(REDIRECT_URL);

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, final OneTimeToken oneTimeToken) throws IOException, ServletException {
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(UrlUtils.buildFullRequestUrl(request))
                .replacePath(request.getContextPath())
                .replaceQuery(null)
                .fragment(null)
                .path("/login/ott")
                .queryParam("token", oneTimeToken.getTokenValue());

        final String magicLink = builder.toUriString();
        LOGGER.info("Magic link: {}", magicLink);

        // Skipped sending email for the user.
        // final String email = getUserEmail(oneTimeToken.getUsername());

        handler.handle(request, response, oneTimeToken);
    }
}
