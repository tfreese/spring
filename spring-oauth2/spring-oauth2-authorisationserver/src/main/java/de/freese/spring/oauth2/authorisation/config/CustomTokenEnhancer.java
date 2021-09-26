// Created: 06.11.2019
package de.freese.spring.oauth2.authorisation.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.util.CollectionUtils;

/**
 * @author Thomas Freese
 */
public class CustomTokenEnhancer implements TokenEnhancer
{
    /**
     * @see org.springframework.security.oauth2.provider.token.TokenEnhancer#enhance(org.springframework.security.oauth2.common.OAuth2AccessToken,
     *      org.springframework.security.oauth2.provider.OAuth2Authentication)
     */
    @Override
    public OAuth2AccessToken enhance(final OAuth2AccessToken accessToken, final OAuth2Authentication authentication)
    {
        DefaultOAuth2AccessToken defaultOAuth2AccessToken = (DefaultOAuth2AccessToken) accessToken;

        Map<String, Object> additionalInfo = defaultOAuth2AccessToken.getAdditionalInformation();

        if (CollectionUtils.isEmpty(additionalInfo))
        {
            additionalInfo = new HashMap<>();
        }

        additionalInfo.put("organization", authentication.getName() + "-MyOrga");

        defaultOAuth2AccessToken.setAdditionalInformation(additionalInfo);

        return accessToken;
    }
}
