// Created: 22.12.2021
package de.freese.spring.jwt.token.nimbus;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import de.freese.spring.jwt.token.JwtToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * @author Thomas Freese
 */
public class JwtTokenNimbus implements JwtToken
{
    private final JWT jwt;

    public JwtTokenNimbus(final JWT jwt)
    {
        super();

        this.jwt = Objects.requireNonNull(jwt, "jwt required");
    }

    /**
     * @see de.freese.spring.jwt.token.JwtToken#getExpirationDate()
     */
    @Override
    public Date getExpirationDate()
    {
        return getClaimsValue(JWTClaimsSet::getExpirationTime);
    }

    /**
     * @see de.freese.spring.jwt.token.JwtToken#getPassword()
     */
    @Override
    public String getPassword()
    {
        return getClaimsValue(jwtClaims -> (String) jwtClaims.getClaim("password"));
    }

    /**
     * @see de.freese.spring.jwt.token.JwtToken#getRoles()
     */
    @Override
    public Set<GrantedAuthority> getRoles()
    {
        String rolesString = getClaimsValue(jwtClaims -> (String) jwtClaims.getClaim("roles"));

        if ((rolesString == null) || rolesString.isBlank())
        {
            return Collections.emptySet();
        }

        String[] rolesArray = rolesString.split(",");

        return Arrays.stream(rolesArray).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    /**
     * @see de.freese.spring.jwt.token.JwtToken#getUsername()
     */
    @Override
    public String getUsername()
    {
        return getClaimsValue(JWTClaimsSet::getSubject);
    }

    private <T> T getClaimsValue(final Function<JWTClaimsSet, T> function)
    {
        try
        {
            return function.apply(this.jwt.getJWTClaimsSet());
        }
        catch (ParseException ex)
        {
            throw new AuthenticationServiceException(ex.getMessage());
        }
    }
}
