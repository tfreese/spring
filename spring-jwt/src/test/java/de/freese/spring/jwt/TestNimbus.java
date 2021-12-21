// Created: 21.12.2021
package de.freese.spring.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.nimbusds.jose.EncryptionMethod;
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
import com.nimbusds.jwt.PlainJWT;

/**
 * @author Thomas Freese
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class TestNimbus
{
    /**
     *
     */
    private static final String SECRET_KEY = "gehaim";
    /**
    *
    */
    private static final AtomicReference<String> TOKEN_ENCRYPTED = new AtomicReference<>(null);
    /**
     *
     */
    private static final AtomicReference<String> TOKEN_PLAIN = new AtomicReference<>(null);

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void test01CreateToken() throws Exception
    {
        String userName = "user";
        String password = "pass";

        Date now = new Date();
        Date expiration = new Date(now.getTime() + 36000);

        Builder builder = new JWTClaimsSet.Builder();

        if ((password != null) && !password.isBlank())
        {
            builder = builder.claim("password", password);
        }

        // @formatter:off
        JWTClaimsSet jwtClaims = builder
                .issuer("tommy")
                .subject(userName)
                .expirationTime(expiration)
                .notBeforeTime(now)
                .issueTime(now)
                .jwtID(UUID.randomUUID().toString())
                .build()
                ;
        // @formatter:on

        JWT jwt = new PlainJWT(jwtClaims);
        TOKEN_PLAIN.set(jwt.serialize());

        System.out.println(jwt.getJWTClaimsSet());
        System.out.println(TOKEN_PLAIN.get());

        // Verschlüsseln
        JWEEncrypter encrypter = new PasswordBasedEncrypter(SECRET_KEY, 8, 1000);

        JWEHeader header = new JWEHeader(JWEAlgorithm.PBES2_HS512_A256KW, EncryptionMethod.A256CBC_HS512);
        JWT encryptedJWT = new EncryptedJWT(header, jwtClaims);

        ((EncryptedJWT) encryptedJWT).encrypt(encrypter);
        TOKEN_ENCRYPTED.set(encryptedJWT.serialize());

        System.out.println(encryptedJWT.getJWTClaimsSet());
        System.out.println(TOKEN_ENCRYPTED.get());

        assertTrue(true);
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void test02ParseTokenPlain() throws Exception
    {
        PlainJWT jwt = PlainJWT.parse(TOKEN_PLAIN.get());

        JWTClaimsSet jwtClaims = jwt.getJWTClaimsSet();

        assertEquals("tommy", jwtClaims.getIssuer());
        assertEquals("user", jwtClaims.getSubject());
        assertEquals("pass", jwtClaims.getClaim("password"));
        assertTrue(jwtClaims.getExpirationTime().after(new Date()));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void test03ParseTokenEncrypted() throws Exception
    {
        EncryptedJWT jwt = EncryptedJWT.parse(TOKEN_ENCRYPTED.get());

        JWEDecrypter decrypter = new PasswordBasedDecrypter(SECRET_KEY);
        jwt.decrypt(decrypter);

        JWTClaimsSet jwtClaims = jwt.getJWTClaimsSet();

        assertEquals("tommy", jwtClaims.getIssuer());
        assertEquals("user", jwtClaims.getSubject());
        assertEquals("pass", jwtClaims.getClaim("password"));
        assertTrue(jwtClaims.getExpirationTime().after(new Date()));
    }
}
