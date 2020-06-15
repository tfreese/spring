/**
 * Created: 14.09.2018
 */

package de.freese.spring.thymeleaf.rest;

/**
 * @author Thomas Freese
 */
abstract class AbstractRestTestCase
{
    /**
     * @throws Exception Falls was schief geht.
     */
    abstract void test000HealthEndpoint() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    abstract void test010UserWithoutLogin() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    abstract void test011UserWithWrongPass() throws Exception;

    /**
     * User "invalid" hat keine Berechtigung für "person/personList".
     *
     * @throws Exception Falls was schief geht.
     */
    abstract void test020UserWithWrongRole() throws Exception;

    /**
     * User "invalid" hat keine Berechtigung für "person/personList".
     *
     * @throws Exception Falls was schief geht. abstract void test020UserWithWrongRole() throws Exception; /**
     * @throws Exception Falls was schief geht.
     */
    abstract void test030UserWithLoginJSON() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    abstract void test031UserWithLoginXML() throws Exception;

    /**
     * User "user" hat keine Berechtigung für "person/personAdd".
     *
     * @throws Exception Falls was schief geht.
     */
    abstract void test040PostWithWrongRole() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    abstract void test041Post() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    abstract void test050UserWithPreAuthJSON() throws Exception;

    /**
     * @throws Exception Falls was schief geht.
     */
    abstract void test051UserWithPreAuthXML() throws Exception;
}