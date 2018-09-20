/**
 * Created: 14.09.2018
 */

package de.freese.spring.thymeleaf.rest;

/**
 * @author Thomas Freese
 */
public interface RestTestCase
{
    /**
     * @throws Exception Falls was schief geht.
     */
    public void test000HealthEndpoint() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    public void test010UserWithoutLogin() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    public void test011UserWithWrongPass() throws Exception;

    /**
     * User "invalid" hat keine Berechtigung für "person/personList".
     *
     * @throws Exception Falls was schief geht.
     */
    public void test020UserWithWrongRole() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    public void test030UserWithLoginJSON() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    public void test031UserWithLoginXML() throws Exception;

    /**
     * User "user" hat keine Berechtigung für "person/personAdd".
     *
     * @throws Exception Falls was schief geht.
     */
    public void test040PostWithWrongRole() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    public void test041Post() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    public void test050UserWithPreAuthJSON() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    public void test051UserWithPreAuthXML() throws Exception;
}