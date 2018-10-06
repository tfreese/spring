/**
 *
 */
package de.freese.spring.thymeleaf;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import de.freese.spring.thymeleaf.rest.TestRestWithJreHttpClient;
import de.freese.spring.thymeleaf.rest.TestRestWithMockMvc;
import de.freese.spring.thymeleaf.rest.TestRestWithRestTemplate;
import de.freese.spring.thymeleaf.rest.TestRestWithRestTemplateSSL;
import de.freese.spring.thymeleaf.rest.TestRestWithWebClient;
import de.freese.spring.thymeleaf.rest.TestRestWithWebClientSSL;
import de.freese.spring.thymeleaf.web.TestWebApp;

/**
 * @author Thomas Freese
 */
@RunWith(Suite.class)
@SuiteClasses(
{
    // @formatter:off
        TestPasswordEncoder.class,
        TestWebApp.class,
        TestRestWithMockMvc.class,
        TestRestWithRestTemplate.class,
        TestRestWithRestTemplateSSL.class,
        TestRestWithWebClient.class,
        TestRestWithWebClientSSL.class,
        TestRestWithJreHttpClient.class
    // @formatter:off
})
public class AllThymeleafTests
{
    // /**
    // *
    // */
    // private static SimpleNamingContextBuilder namingContext = null;

    // /**
    // *
    // */
    // @AfterClass
    // public static void afterClass()
    // {
    // namingContext.clear();
    // }

    // /**
    // * @throws Exception Falls was schief geht.
    // */
    // @BeforeClass
    // public static void beforeClass() throws Exception
    // {
    // namingContext = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
    // // namingContext.bind("java:comp/env/jdbc/spring/manualTX", new TestConfig().dataSource());
    // }

    /**
     * Erzeugt eine neue Instanz von {@link AllThymeleafTests}
     */
    public AllThymeleafTests()
    {
        super();
    }
    // /**
    // * In der Methode werden alle Testklassen registriert die durch JUnit aufgerufen werden sollen.
    // *
    // * @return {@link Test}
    // */
    // public static Test suite()
    // {
    // TestSuite suite = new TestSuite("de.freese.jdbc");
    //
    // suite.addTest(new JUnit4TestAdapter(TestJdbcDao.class));
    //
    // return suite;
    // }
}
