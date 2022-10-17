package de.freese.spring.rsocket;

import java.sql.Date;
import java.util.Optional;
import java.util.UUID;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.PasswordBasedEncrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketClient;
import io.rsocket.metadata.WellKnownMimeType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.rsocket.server.LocalRSocketServerPort;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.security.rsocket.metadata.BearerTokenMetadata;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Hooks;

/**
 * @author Thomas Freese
 */
@ActiveProfiles(
        {
                "test", "jwt"
        })
class TestClientToServerJwt implements TestClientToServer
{
    private static RSocketRequester requester;

    @AfterAll
    public static void afterAll()
    {
        Optional.ofNullable(requester.rsocketClient()).ifPresent(RSocketClient::dispose);
        Optional.ofNullable(requester.rsocket()).ifPresent(RSocket::dispose);
    }

    @BeforeAll
    public static void beforeAll(@Autowired final RSocketRequester.Builder builder, @Autowired final RSocketStrategies strategies,
                                 @Value("${spring.rsocket.server.address}") final String host, @LocalRSocketServerPort final int port)
            throws Exception
    {
        // Fehlermeldung, wenn Client die Verbindung schliesst.
        // Nur einmalig definieren, sonst gib es mehrere Logs-Meldungen !!!
        // Hooks.onErrorDropped(th -> LOGGER.warn(th.getMessage()));
        Hooks.onErrorDropped(th ->
        {
            // Empty
        });

        // @formatter:off
        JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder()
                .issuer("test-app")
                .subject("user")
                .claim("password", "pass")
                .expirationTime(new Date(System.currentTimeMillis() + 3_600_000))
                .jwtID(UUID.randomUUID().toString())
                .build()
                ;
        // @formatter:on

        JWEEncrypter encrypter = new PasswordBasedEncrypter("my-password", 8, 1000);
        JWEHeader jweHeader = new JWEHeader(JWEAlgorithm.PBES2_HS512_A256KW, EncryptionMethod.A256CBC_HS512);
        EncryptedJWT encryptedJWT = new EncryptedJWT(jweHeader, jwtClaims);
        encryptedJWT.encrypt(encrypter);

        String token = encryptedJWT.serialize();

        BearerTokenMetadata credentials = new BearerTokenMetadata(token);
        MimeType mimeType = MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());

        // .setupMetadata(token, BearerTokenMetadata.BEARER_AUTHENTICATION_MIME_TYPE)

        // @formatter:off
        requester = builder
                .setupMetadata(credentials, mimeType)
                .tcp(host, port)
                ;
        // @formatter:on
    }

    /**
     * @see de.freese.spring.rsocket.TestClientToServer#getRequester()
     */
    @Override
    public RSocketRequester getRequester()
    {
        return requester;
    }
}
