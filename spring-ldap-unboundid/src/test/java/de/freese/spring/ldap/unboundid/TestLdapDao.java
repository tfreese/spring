// Created: 17.02.2019
package de.freese.spring.ldap.unboundid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jakarta.annotation.Resource;

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
class TestLdapDao {
    @Resource
    private MyLdapDao ldapDao;

    @Test
    void testContextLoads() {
        assertTrue(true);
    }

    @Test
    void testCreate() {
        ldapDao.create("myid", "pass", "A", "B");

        final List<String> result = ldapDao.searchPeopleByUid("myid");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("A B", result.getFirst());
    }

    @Test
    void testModify() {
        ldapDao.modify("myid", "pass", "X", "Y");

        final List<String> result = ldapDao.searchPeopleByUid("myid");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("X Y", result.getFirst());
    }

    @Test
    void testSearchGroup() {
        final List<String> result = ldapDao.searchGroup("user");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("uid=admin,ou=people,dc=springframework,dc=org", result.get(0));
        assertEquals("uid=user,ou=people,dc=springframework,dc=org", result.get(1));
    }

    @Test
    void testSearchPeople() {
        final List<String> result = ldapDao.searchPeopleByUid("u*");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dianne Emu", result.getFirst());
    }
}
