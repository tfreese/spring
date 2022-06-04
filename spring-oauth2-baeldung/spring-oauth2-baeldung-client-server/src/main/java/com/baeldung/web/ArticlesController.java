package com.baeldung.web;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

import javax.annotation.Resource;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

/**
 *
 */
@RestController
public class ArticlesController
{
    /**
     *
     */
    @Resource
    private WebClient webClient;

    /**
     * @param authorizedClient {@link OAuth2AuthorizedClient}
     *
     * @return String[]
     */
    @GetMapping(value = "/articles")
    public String[] getArticles(@RegisteredOAuth2AuthorizedClient("articles-client-authorization-code") final OAuth2AuthorizedClient authorizedClient)
    {
        // @formatter:off
        return this.webClient
                .get()
                .uri("http://localhost:8090/articles")
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .bodyToMono(String[].class)
                .block()
                ;
        // @formatter:on
    }
}
