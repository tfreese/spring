/**
 * Created: 17.02.2019
 */

package de.freese.spring.ldap;

import java.util.List;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import de.freese.spring.ldap.dao.MyLdapDao;

/**
 * @author Thomas Freese
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
        Assert.assertEquals("Ben Alex", principal);

        principal = this.ldapDao.login("uid=joe,ou=otherpeople", "joespassword");
        Assert.assertEquals("Joe Smeth", principal);
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test030SearchPeople() throws Exception
    {
        List<String> result = this.ldapDao.searchPeople("b*");

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("Ben Alex", result.get(0));
        Assert.assertEquals("Bob Hamilton", result.get(1));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test030SearchPeopleInGroup() throws Exception
    {
        List<String> result = this.ldapDao.searchPeopleInGroup("developers");

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("uid=ben,ou=people,dc=springframework,dc=org", result.get(0));
        Assert.assertEquals("uid=bob,ou=people,dc=springframework,dc=org", result.get(1));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test040Create() throws Exception
    {
        this.ldapDao.create("myid", "pass", "A", "B");
        List<String> result = this.ldapDao.searchPeople("myid");

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("A B", result.get(0));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test050Modify() throws Exception
    {
        this.ldapDao.modify("myid", "pass", "X", "Y");
        List<String> result = this.ldapDao.searchPeople("myid");

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("X Y", result.get(0));
    }
}
