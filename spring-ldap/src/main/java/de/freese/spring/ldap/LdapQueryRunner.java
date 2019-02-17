/**
 * Created: 17.02.2019
 */

package de.freese.spring.ldap;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import javax.annotation.Resource;
import javax.naming.Name;
import javax.naming.directory.DirContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Component;

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
    @Value("${spring.ldap.embedded.base-dn}")
    private String baseDn = null;

    /**
    *
    */
    @Resource
    private Environment env = null;

    /**
     *
     */
    @Resource
    private LdapTemplate ldapTemplate = null;

    /**
     * Erstellt ein neues {@link LdapQueryRunner} Object.
     */
    public LdapQueryRunner()
    {
        super();
    }

    /**
     * uid=bob,ou=people,dc=springframework,dc=org
     *
     * @param ldapTemplate {@link LdapTemplate}
     * @param userId String
     * @param password String
     * @param firstName String
     * @param lastName String
     */
    public void create(final LdapTemplate ldapTemplate, final String userId, final String password, final String firstName, final String lastName)
    {
        Name dn = LdapNameBuilder.newInstance().add("dc", "org").add("dc", "springframework").add("ou", "people").add("uid", userId).build();
        DirContextAdapter context = new DirContextAdapter(dn);

        context.setAttributeValues("objectclass", new String[]
        {
                "top", "person", "organizationalPerson", "inetOrgPerson"
        });
        context.setAttributeValue("cn", firstName + " " + lastName);
        context.setAttributeValue("sn", lastName);
        context.setAttributeValue("userPassword", digestSHA(password));

        ldapTemplate.bind(context);
    }

    /**
     * @param password String
     * @return String
     */
    private String digestSHA(final String password)
    {
        String base64 = null;

        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA");
            digest.update(password.getBytes());
            base64 = Base64.getEncoder().encodeToString(digest.digest());
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new RuntimeException(ex);
        }

        return "{SHA}" + base64;
    }

    /**
     * @param ldapTemplate {@link LdapTemplate}
     * @param baseDn String
     * @param userName String
     * @param password String
     * @return String
     * @throws Exception Falls was schief geht.
     */
    public String login(final LdapTemplate ldapTemplate, final String baseDn, final String userName, final String password) throws Exception
    {
        // "cn=" +
        DirContext dirContext = ldapTemplate.getContextSource().getContext(userName + "," + baseDn, password);

        return (String) dirContext.getEnvironment().get("java.naming.security.principal");
    }

    /**
     * @param ldapTemplate {@link LdapTemplate}
     * @param userId String
     * @param password String
     * @param firstName String
     * @param lastName String
     */
    public void modify(final LdapTemplate ldapTemplate, final String userId, final String password, final String firstName, final String lastName)
    {
        Name dn = LdapNameBuilder.newInstance().add("dc", "org").add("dc", "springframework").add("ou", "people").add("uid", userId).build();
        DirContextOperations context = ldapTemplate.lookupContext(dn);

        context.setAttributeValues("objectclass", new String[]
        {
                "top", "person", "organizationalPerson", "inetOrgPerson"
        });
        context.setAttributeValue("cn", firstName + " " + lastName);
        context.setAttributeValue("sn", lastName);
        context.setAttributeValue("userPassword", digestSHA(password));

        ldapTemplate.modifyAttributes(context);
    }

    /**
     * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
     */
    @Override
    public void run(final String...args) throws Exception
    {
        String userName = "uid=ben,ou=people";
        String password = "{SHA}nFCebWjxfaLbHHG1Qk5UU4trbvQ=";

        String prinzipal = login(this.ldapTemplate, this.baseDn, userName, password);
        System.out.println(prinzipal);

        search(this.ldapTemplate, this.baseDn, "b*").forEach(System.out::println);

        create(this.ldapTemplate, "myid", "pass", "A", "B");
        search(this.ldapTemplate, this.baseDn, "myid").forEach(System.out::println);

        modify(this.ldapTemplate, "myid", "pass", "X", "Y");
        search(this.ldapTemplate, this.baseDn, "myid").forEach(System.out::println);

        // System.exit(0);
    }

    /**
     * @param ldapTemplate {@link LdapTemplate}
     * @param baseDn String
     * @param userId String
     * @return {@link List}
     */
    public List<String> search(final LdapTemplate ldapTemplate, final String baseDn, final String userId)
    {
        // return ldapTemplate.search("ou=people," + baseDn, "uid=" + userId, (AttributesMapper<String>) attrs -> (String) attrs.get("cn").get());

        // @formatter:off
        LdapQuery query = LdapQueryBuilder.query()
                .searchScope(SearchScope.SUBTREE)
                .timeLimit(3 * 1000)
                .countLimit(10)
                .attributes("cn")
                .base("ou=people," + baseDn)
                .where("objectclass").is("person")
                .and("uid").isPresent()
                .and("uid").like(userId)
                //.and("sn").not().is(lastName)
                ;
        // @formatter:on

        return ldapTemplate.search(query, (AttributesMapper<String>) attrs -> (String) attrs.get("cn").get());
    }
}
