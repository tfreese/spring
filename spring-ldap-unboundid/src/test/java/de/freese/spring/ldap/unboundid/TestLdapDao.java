// Created: 17.02.2019
package de.freese.spring.ldap.unboundid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import de.freese.spring.ldap.unboundid.dao.MyLdapDao;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = LdapApplication.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@ActiveProfiles("test")
class TestLdapDao
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
    void testSearchPeople() throws Exception
    {
        List<String> result = this.ldapDao.searchPeopleByUid("u*");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dianne Emu", result.get(0));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @Test
    void testSearchGroup() throws Exception
    {
        List<String> result = this.ldapDao.searchGroup("user");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("uid=admin,ou=people,dc=springframework,dc=org", result.get(0));
        assertEquals("uid=user,ou=people,dc=springframework,dc=org", result.get(1));
    }
}
