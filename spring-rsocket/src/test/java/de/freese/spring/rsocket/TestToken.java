// Created: 17.02.2022
package de.freese.spring.rsocket;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.PasswordBasedEncrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Freese
 */
class TestToken {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestToken.class);

    @Test
    void createToken() throws Exception {
        // final Mac mac = Mac.getInstance("HmacSHA256");
        // final SecretKeySpec secretKey = new SecretKeySpec("my-secret".getBytes(), mac.getAlgorithm());

        // final JWSAlgorithm jwsAlgorithm = JWSAlgorithm.parse(MacAlgorithm.HS256.getName());
        //
        // final JWSKeySelector<SecurityContext> jwsKeySelector = new SingleKeyJWSKeySelector<>(jwsAlgorithm, secretKey);
        //
        // final DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        // jwtProcessor.setJWSKeySelector(jwsKeySelector);
        // jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {
        // });

        final JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder()
                //.issuer("test-app")
                .subject("user")
                .claim("password", "pass")
                .expirationTime(Date.from(LocalDateTime.now().plusMinutes(60).atZone(ZoneId.systemDefault()).toInstant()))
                //.jwtID(UUID.randomUUID().toString())
                .build();

        final PlainJWT plainJWT = new PlainJWT(jwtClaims);
        LOGGER.info(plainJWT.serialize());

        // final JWSSigner jwsSigner = new MACSigner(secretKey);
        // final JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        // final SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaims);
        // signedJWT.sign(jwsSigner);
        // System.out.println(signedJWT.serialize());

        final JWEEncrypter encrypter = new PasswordBasedEncrypter("my-password", 8, 1000);
        final JWEHeader jweHeader = new JWEHeader(JWEAlgorithm.PBES2_HS512_A256KW, EncryptionMethod.A256CBC_HS512);
        final EncryptedJWT encryptedJWT = new EncryptedJWT(jweHeader, jwtClaims);
        encryptedJWT.encrypt(encrypter);
        LOGGER.info(encryptedJWT.serialize());

        assertTrue(true);
    }
}
