/**
 * Created: 17.02.2019
 */

package de.freese.spring.ldap;

import javax.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import de.freese.spring.ldap.dao.MyLdapDao;

/**
 * @author Thomas Freese
 */
@Component
@Profile("!test")
@Order(10)
public class LdapQueryRunner implements CommandLineRunner
{
    /**
     *
     */
    @Resource
    private MyLdapDao ldapDao = null;

    /**
     * Erstellt ein neues {@link LdapQueryRunner} Object.
     */
    public LdapQueryRunner()
    {
        super();
    }

    /**
     * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
     */
    @Override
    public void run(final String...args) throws Exception
    {
        String userName = "uid=ben,ou=people";
        String password = "{SHA}nFCebWjxfaLbHHG1Qk5UU4trbvQ=";

        String prinzipal = this.ldapDao.login(userName, password);
        System.out.println(prinzipal);

        this.ldapDao.searchPeopleByUid("b*").forEach(System.out::println);

        this.ldapDao.create("myid", "pass", "A", "B");
        this.ldapDao.searchPeopleByUid("myid").forEach(System.out::println);

        this.ldapDao.modify("myid", "pass", "X", "Y");
        this.ldapDao.searchPeopleByUid("myid").forEach(System.out::println);
    }
}
