/**
 * Created: 06.11.2019
 */

package de.freese.spring.oauth2.authorisation.config;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Thomas Freese
 */
@FrameworkEndpoint
public class RevokeTokenEndpoint
{
    /**
     *
     */
    @Resource(name = "tokenServices")
    private ConsumerTokenServices tokenServices;

    /**
     * Erstellt ein neues {@link RevokeTokenEndpoint} Object.
     */
    public RevokeTokenEndpoint()
    {
        super();
    }

    /**
     * @param request {@link HttpServletRequest}
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/oauth/token")
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
