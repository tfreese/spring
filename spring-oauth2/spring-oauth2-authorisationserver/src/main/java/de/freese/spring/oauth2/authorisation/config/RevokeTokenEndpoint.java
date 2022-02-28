// Created: 06.11.2019
package de.freese.spring.oauth2.authorisation.config;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Abmelden mit OAuth2.
 *
 * @author Thomas Freese
 */
@FrameworkEndpoint
class RevokeTokenEndpoint
{
    /**
     *
     */
    @Resource(name = "tokenServices")
    private ConsumerTokenServices tokenServices;

    /**
     * @param request {@link HttpServletRequest}
     */
    // @RequestMapping(method = RequestMethod.DELETE, value = "/oauth/token")
    @DeleteMapping("/oauth/token")
    @ResponseBody
    public void revokeToken(final HttpServletRequest request)
    {
        String authorization = request.getHeader("Authorization");

        if ((authorization != null) && authorization.contains("Bearer"))
        {
            String tokenId = authorization.substring("Bearer".length() + 1);

            this.tokenServices.revokeToken(tokenId);
        }
    }
}
