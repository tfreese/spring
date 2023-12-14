// Created: 22.12.2021
package de.freese.spring.jwt.token.nimbus;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.PasswordBasedDecrypter;
import com.nimbusds.jose.crypto.PasswordBasedEncrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;

import de.freese.spring.jwt.token.JwtToken;
import de.freese.spring.jwt.token.JwtTokenProvider;

/**
 * @author Thomas Freese
 */
public class JwtTokenProviderNimbus implements JwtTokenProvider {
    private final JWEDecrypter decrypter;
    private final JWEEncrypter encrypter;
    private final long validityInMilliseconds;

    public JwtTokenProviderNimbus(final long validityInMilliseconds, final String secretKey) {
        super();

        this.validityInMilliseconds = validityInMilliseconds;

        this.encrypter = new PasswordBasedEncrypter(secretKey, 8, 1000);
        this.decrypter = new PasswordBasedDecrypter(secretKey);
    }

    public JwtTokenProviderNimbus(final long validityInMilliseconds, final SecretKey secretKey) {
        super();

        this.validityInMilliseconds = validityInMilliseconds;

        try {
            this.encrypter = new DirectEncrypter(secretKey);
            this.decrypter = new DirectDecrypter(secretKey);
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String createToken(final String username, final String password, final Set<String> roles) {
        Builder builder = new JWTClaimsSet.Builder();

        if ((password != null) && !password.isBlank()) {
            builder = builder.claim("password", password);
        }

        if ((roles != null) && !roles.isEmpty()) {
            // @formatter:off
            final String rolesString = roles.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining(","))
                    ;
            // @formatter:on

            builder = builder.claim("roles", rolesString);
        }

        final Date now = new Date();
        final Date expiration = new Date(now.getTime() + this.validityInMilliseconds);

        // @formatter:off
        final JWTClaimsSet jwtClaims = builder
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
        final JWEHeader header = new JWEHeader(JWEAlgorithm.PBES2_HS512_A256KW, EncryptionMethod.A256CBC_HS512);
        final EncryptedJWT encryptedJWT = new EncryptedJWT(header, jwtClaims);

        try {
            encryptedJWT.encrypt(encrypter);
        }
        catch (IllegalStateException ex) {
            throw new AuthenticationServiceException("Token is not in an unencrypted state");
        }
        catch (JOSEException ex) {
            throw new AuthenticationServiceException("Token couldn't be encrypted");
        }

        return encryptedJWT.serialize();
    }

    @Override
    public JwtToken parseToken(final String token) throws AuthenticationException {
        try {
            final EncryptedJWT encryptedJWT = EncryptedJWT.parse(token);
            encryptedJWT.decrypt(decrypter);

            // JWTClaimsSet jwtClaims = encryptedJWT.getJWTClaimsSet();
            // JWT jwt = new PlainJWT(jwtClaims);

            return new JwtTokenNimbus(encryptedJWT);
        }
        catch (ParseException ex) {
            throw new AuthenticationServiceException("JwtToken couldn't be parsed to a valid encrypted JWT");
        }
        catch (JOSEException ex) {
            throw new AuthenticationServiceException("Token couldn't be decrypted");
        }
    }
}
