/// **
// * Created: 06.11.2019
// */
//
// package de.freese.spring.oauth2.client.rest;
//
// import java.io.IOException;
// import org.springframework.http.HttpRequest;
// import org.springframework.http.client.ClientHttpRequestExecution;
// import org.springframework.http.client.ClientHttpRequestInterceptor;
// import org.springframework.http.client.ClientHttpResponse;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
// import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
//
/// **
// * @author Thomas Freese
// */
// public class AuthorizationHeaderInterceptor implements ClientHttpRequestInterceptor
// {
// /**
// *
// */
// private final OAuth2AuthorizedClientService clientService;
//
// /**
// * Erstellt ein neues {@link AuthorizationHeaderInterceptor} Object.
// *
// * @param clientService {@link OAuth2AuthorizedClientService}
// */
// public AuthorizationHeaderInterceptor(final OAuth2AuthorizedClientService clientService)
// {
// super();
//
// this.clientService = clientService;
// }
//
// /**
// * @see org.springframework.http.client.ClientHttpRequestInterceptor#intercept(org.springframework.http.HttpRequest, byte[],
// * org.springframework.http.client.ClientHttpRequestExecution)
// */
// @Override
// public ClientHttpResponse intercept(final HttpRequest request, final byte[] bytes, final ClientHttpRequestExecution execution) throws IOException
// {
// // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
// // String accessToken = null;
// //
// // if ((authentication != null) && authentication.getClass().isAssignableFrom(OAuth2AuthenticationToken.class))
// // {
// // OAuth2AuthenticationToken auth = (OAuth2AuthenticationToken) authentication;
// // String clientRegistrationId = auth.getAuthorizedClientRegistrationId();
// //
// // OAuth2AuthorizedClient client = this.clientService.loadAuthorizedClient(clientRegistrationId, auth.getName());
// // accessToken = client.getAccessToken().getTokenValue();
// //
// // request.getHeaders().add("Authorization", "Bearer " + accessToken);
// // }
// //
// // return execution.execute(request, bytes);
//
// Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
// if (authentication == null)
// {
// String clientRegistrationId = "custom-client";
//
// OAuth2AuthorizedClient client = this.clientService.loadAuthorizedClient(clientRegistrationId, "my-app");
// String accessToken = client.getAccessToken().getTokenValue();
//
// request.getHeaders().add("Authorization", "Bearer " + accessToken);
// }
//
// return execution.execute(request, bytes);
// }
// }
