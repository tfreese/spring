/**
 * Created: 17.02.2019
 */
package de.freese.spring.ldap.unboundid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import javax.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import de.freese.spring.ldap.unboundid.dao.MyLdapDao;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = LdapApplication.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@ActiveProfiles("test")
class LdapTest
{
    /**
     *
     */
    @Resource
    private MyLdapDao ldapDao;

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testContextLoads() throws Exception
    {
        assertTrue(true);
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testCreate() throws Exception
    {
        this.ldapDao.create("myid", "pass", "A", "B");
        List<String> result = this.ldapDao.searchPeopleByUid("myid");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("A B", result.get(0));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testLogin() throws Exception
    {
        String principal = this.ldapDao.login("uid=ben,ou=people", "{SHA}nFCebWjxfaLbHHG1Qk5UU4trbvQ=");
        assertEquals("Ben Alex", principal);

        principal = this.ldapDao.login("uid=joe,ou=otherpeople", "joespassword");
        assertEquals("Joe Smeth", principal);
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testModify() throws Exception
    {
        this.ldapDao.modify("myid", "pass", "X", "Y");
        List<String> result = this.ldapDao.searchPeopleByUid("myid");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("X Y", result.get(0));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testSearchBirthday() throws Exception
    {
        List<String> result = this.ldapDao.searchPeopleByUid("tommy", "birthDate");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("1975-05-13", result.get(0));
        assertEquals("1975-05-13", result.get(1));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testSearchPeople() throws Exception
    {
        List<String> result = this.ldapDao.searchPeopleByUid("b*");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Ben Alex", result.get(0));
        assertEquals("Bob Hamilton", result.get(1));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testSearchPeopleInGroup() throws Exception
    {
        List<String> result = this.ldapDao.searchPeopleInGroup("developers");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("uid=ben,ou=people,dc=springframework,dc=org", result.get(0));
        assertEquals("uid=bob,ou=people,dc=springframework,dc=org", result.get(1));
    }
}
