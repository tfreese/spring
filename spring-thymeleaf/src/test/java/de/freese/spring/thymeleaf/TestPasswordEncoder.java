/**
 * Created: 21.01.2018
 */

package de.freese.spring.thymeleaf;

import java.security.SecureRandom;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;

/**
 * @author Thomas Freese
 */
@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestPasswordEncoder
{
    /**
     *
     */
    private static final Pbkdf2PasswordEncoder pbkdf2_SHA1 = new Pbkdf2PasswordEncoder("mySecret");

    /**
     *
     */
    private static final Pbkdf2PasswordEncoder pbkdf2_SHA512 = new Pbkdf2PasswordEncoder("mySecret");

    /**
     *
     */
    @BeforeClass
    public static void beforeClass()
    {
        pbkdf2_SHA512.setAlgorithm(SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA512);
        pbkdf2_SHA512.setEncodeHashAsBase64(false);
    }

    /**
     * @return {@link Iterable}
     * @throws Exception Falls was schief geht.
     */
    @Parameters(name = "PasswordEncoder: {0}")
    public static Iterable<Object[]> stemmer() throws Exception
    {
        return Arrays.asList(new Object[][]
        {
                {
                        "BCrypt", new BCryptPasswordEncoder(10, new SecureRandom())
                },
                {
                        "Pbkdf2_SHA1", pbkdf2_SHA1
                },
                {
                        "Pbkdf2_SHA512", pbkdf2_SHA512
                }
        });
    }

    /**
    *
    */
    private final PasswordEncoder passwordEncoder;

    /**
     * Erstellt ein neues {@link TestPasswordEncoder} Object.
     *
     * @param name String
     * @param passwordEncoder {@link PasswordEncoder}
     */
    public TestPasswordEncoder(final String name, final PasswordEncoder passwordEncoder)
    {
        super();

        this.passwordEncoder = passwordEncoder;
    }

    /**
     *
     */
    @Test
    public void testPasswordEncoder()
    {
        String password = "gehaim";

        String encoded = this.passwordEncoder.encode(password);
        System.out.println(encoded);

        Assert.assertTrue(this.passwordEncoder.matches(password, encoded));
    }
}
