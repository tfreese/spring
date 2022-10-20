// Created: 21.01.2018
package de.freese.spring.thymeleaf;

import java.security.SecureRandom;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;

/**
 * @author Thomas Freese
 */
// @TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TestPasswordEncoder
{
    private static final Pbkdf2PasswordEncoder pbkdf2_SHA1 = new Pbkdf2PasswordEncoder("mySecret");

    private static final Pbkdf2PasswordEncoder pbkdf2_SHA512 = new Pbkdf2PasswordEncoder("mySecret");

    @BeforeAll
    static void beforeClass()
    {
        pbkdf2_SHA512.setAlgorithm(SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA512);
        pbkdf2_SHA512.setEncodeHashAsBase64(false);
    }

    static Stream<Arguments> createPasswordEncoder()
    {
        // @formatter:off
        return Stream.of(
                Arguments.of("BCrypt", new BCryptPasswordEncoder(10, new SecureRandom())),
                Arguments.of("Pbkdf2_SHA1", pbkdf2_SHA1),
                Arguments.of("Pbkdf2_SHA512", pbkdf2_SHA512)
                );
        // @formatter:on
    }

    @ParameterizedTest(name = "{index} -> {0}")
    @MethodSource("createPasswordEncoder")
    @DisplayName("Test PasswordEncoder")
    void testPasswordEncoder(final String name, final PasswordEncoder passwordEncoder)
    {
        String password = "gehaim";

        String encoded = passwordEncoder.encode(password);

        Assertions.assertTrue(passwordEncoder.matches(password, encoded));
    }
}
