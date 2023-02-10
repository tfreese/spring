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

/**
 * @author Thomas Freese
 */
class TestToken {
    @Test
    void createToken() throws Exception {
        // Mac mac = Mac.getInstance("HmacSHA256");
        // SecretKeySpec secretKey = new SecretKeySpec("my-secret".getBytes(), mac.getAlgorithm());

        // JWSAlgorithm jwsAlgorithm = JWSAlgorithm.parse(MacAlgorithm.HS256.getName());
        //
        // JWSKeySelector<SecurityContext> jwsKeySelector = new SingleKeyJWSKeySelector<>(jwsAlgorithm, secretKey);
        //
        // DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        // jwtProcessor.setJWSKeySelector(jwsKeySelector);
        // jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {
        // });

        // @formatter:off
        JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder()
                //.issuer("test-app")
                .subject("user")
                .claim("password", "pass")
                .expirationTime(Date.from(LocalDateTime.now().plusMinutes(60).atZone(ZoneId.systemDefault()).toInstant()))
                //.jwtID(UUID.randomUUID().toString())
                .build()
                ;
        // @formatter:on

        PlainJWT plainJWT = new PlainJWT(jwtClaims);
        System.out.println(plainJWT.serialize());

        // JWSSigner jwsSigner = new MACSigner(secretKey);
        // JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        // SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaims);
        // signedJWT.sign(jwsSigner);
        // System.out.println(signedJWT.serialize());

        JWEEncrypter encrypter = new PasswordBasedEncrypter("my-password", 8, 1000);
        JWEHeader jweHeader = new JWEHeader(JWEAlgorithm.PBES2_HS512_A256KW, EncryptionMethod.A256CBC_HS512);
        EncryptedJWT encryptedJWT = new EncryptedJWT(jweHeader, jwtClaims);
        encryptedJWT.encrypt(encrypter);
        System.out.println(encryptedJWT.serialize());

        assertTrue(true);
    }
}
