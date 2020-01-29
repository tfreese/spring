/**
 * Created: 17.02.2019
 */

package de.freese.spring.ldap.unboundid;

import java.util.List;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import de.freese.spring.ldap.unboundid.dao.MyLdapDao;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes =
{
        LdapApplication.class
})
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ActiveProfiles("test")
public class LdapTest
{
    /**
     *
     */
    @Resource
    private MyLdapDao ldapDao = null;

    /**
     * Erstellt ein neues {@link LdapTest} Object.
     */
    public LdapTest()
    {
        super();
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test010ContextLoads() throws Exception
    {
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test020Login() throws Exception
    {
        String principal = this.ldapDao.login("uid=ben,ou=people", "{SHA}nFCebWjxfaLbHHG1Qk5UU4trbvQ=");
        Assertions.assertEquals("Ben Alex", principal);

        principal = this.ldapDao.login("uid=joe,ou=otherpeople", "joespassword");
        Assertions.assertEquals("Joe Smeth", principal);
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test030SearchPeople() throws Exception
    {
        List<String> result = this.ldapDao.searchPeopleByUid("b*");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Ben Alex", result.get(0));
        Assertions.assertEquals("Bob Hamilton", result.get(1));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test030SearchPeopleInGroup() throws Exception
    {
        List<String> result = this.ldapDao.searchPeopleInGroup("developers");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("uid=ben,ou=people,dc=springframework,dc=org", result.get(0));
        Assertions.assertEquals("uid=bob,ou=people,dc=springframework,dc=org", result.get(1));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test031SearchBirthday() throws Exception
    {
        List<String> result = this.ldapDao.searchPeopleByUid("tommy", "birthDate");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("1975-05-13", result.get(0));
        Assertions.assertEquals("1975-05-13", result.get(1));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test040Create() throws Exception
    {
        this.ldapDao.create("myid", "pass", "A", "B");
        List<String> result = this.ldapDao.searchPeopleByUid("myid");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("A B", result.get(0));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test050Modify() throws Exception
    {
        this.ldapDao.modify("myid", "pass", "X", "Y");
        List<String> result = this.ldapDao.searchPeopleByUid("myid");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("X Y", result.get(0));
    }
}
