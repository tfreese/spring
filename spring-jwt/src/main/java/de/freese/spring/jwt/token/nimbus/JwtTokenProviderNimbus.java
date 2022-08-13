// Created: 22.12.2021
package de.freese.spring.jwt.token.nimbus;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.PasswordBasedDecrypter;
import com.nimbusds.jose.crypto.PasswordBasedEncrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import de.freese.spring.jwt.token.JwtToken;
import de.freese.spring.jwt.token.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;

/**
 * @author Thomas Freese
 */
public class JwtTokenProviderNimbus implements JwtTokenProvider
{
    /**
     *
     */
    private final String secretKey;
    /**
     *
     */
    private final long validityInMilliseconds;

    /**
     * Erstellt ein neues {@link JwtTokenProviderNimbus} Object.
     *
     * @param secretKey String
     * @param validityInMilliseconds long
     */
    public JwtTokenProviderNimbus(final String secretKey, final long validityInMilliseconds)
    {
        super();

        this.secretKey = secretKey;
        this.validityInMilliseconds = validityInMilliseconds;
    }

    /**
     * @see de.freese.spring.jwt.token.JwtTokenProvider#createToken(java.lang.String, java.lang.String, java.util.Set)
     */
    @Override
    public String createToken(final String username, final String password, final Set<String> roles)
    {
        Builder builder = new JWTClaimsSet.Builder();

        if ((password != null) && !password.isBlank())
        {
            builder = builder.claim("password", password);
        }

        if ((roles != null) && !roles.isEmpty())
        {
            // @formatter:off
            String rolesString = roles.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining(","))
                    ;
            // @formatter:on

            builder = builder.claim("roles", rolesString);
        }

        Date now = new Date();
        Date expiration = new Date(now.getTime() + this.validityInMilliseconds);

        // @formatter:off
        JWTClaimsSet jwtClaims = builder
                .issuer("tommy")
                .subject(username)
                .expirationTime(expiration)
                .notBeforeTime(now)
                .issueTime(now)
                .jwtID(UUID.randomUUID().toString())
                .build()
                ;
        // @formatter:on

        // JWT jwt = new PlainJWT(jwtClaims);

        // Verschlüsseln
        JWEEncrypter encrypter = new PasswordBasedEncrypter(this.secretKey, 8, 1000);

        JWEHeader header = new JWEHeader(JWEAlgorithm.PBES2_HS512_A256KW, EncryptionMethod.A256CBC_HS512);
        JWT encryptedJWT = new EncryptedJWT(header, jwtClaims);

        try
        {
            ((EncryptedJWT) encryptedJWT).encrypt(encrypter);
        }
        catch (IllegalStateException ex)
        {
            throw new AuthenticationServiceException("Token is not in an unencrypted state");
        }
        catch (JOSEException ex)
        {
            throw new AuthenticationServiceException("Token couldn't be encrypted");
        }

        return encryptedJWT.serialize();
    }

    /**
     * @see de.freese.spring.jwt.token.JwtTokenProvider#parseToken(java.lang.String)
     */
    @Override
    public JwtToken parseToken(final String token) throws AuthenticationException
    {
        try
        {
            EncryptedJWT encryptedJWT = EncryptedJWT.parse(token);

            JWEDecrypter decrypter = new PasswordBasedDecrypter(this.secretKey);
            encryptedJWT.decrypt(decrypter);

            // JWTClaimsSet jwtClaims = encryptedJWT.getJWTClaimsSet();
            // JWT jwt = new PlainJWT(jwtClaims);

            return new JwtTokenNimbus(encryptedJWT);
        }
        catch (ParseException ex)
        {
            throw new AuthenticationServiceException("JwtToken couldn't be parsed to a valid encrypted JWT");
        }
        catch (JOSEException ex)
        {
            throw new AuthenticationServiceException("Token couldn't be decrypted");
        }
    }
}
