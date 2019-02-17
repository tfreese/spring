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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

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
    @Value("${spring.ldap.embedded.base-dn}")
    private String baseDn = null;

    /**
     *
     */
    private LdapQueryRunner ldapQueryRunner = new LdapQueryRunner();

    /**
     *
     */
    @Resource
    private LdapTemplate ldapTemplate = null;

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
        String principal = this.ldapQueryRunner.login(this.ldapTemplate, this.baseDn, "uid=ben,ou=people", "{SHA}nFCebWjxfaLbHHG1Qk5UU4trbvQ=");
        Assert.assertEquals(principal, "uid=ben,ou=people,dc=springframework,dc=org");
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test030Search() throws Exception
    {
        List<String> result = this.ldapQueryRunner.search(this.ldapTemplate, this.baseDn, "b*");

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get(0), "Ben Alex");
        Assert.assertEquals(result.get(1), "Bob Hamilton");
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test040Create() throws Exception
    {
        this.ldapQueryRunner.create(this.ldapTemplate, "myid", "pass", "A", "B");
        List<String> result = this.ldapQueryRunner.search(this.ldapTemplate, this.baseDn, "myid");

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0), "A B");
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    public void test050Modify() throws Exception
    {
        this.ldapQueryRunner.modify(this.ldapTemplate, "myid", "pass", "X", "Y");
        List<String> result = this.ldapQueryRunner.search(this.ldapTemplate, this.baseDn, "myid");

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0), "X Y");
    }
}
