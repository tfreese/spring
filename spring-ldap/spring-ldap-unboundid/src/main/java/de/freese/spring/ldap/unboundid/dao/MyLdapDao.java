// Created: 18.02.2019
package de.freese.spring.ldap.unboundid.dao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.core.support.BaseLdapNameAware;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Repository;

/**
 * @author Thomas Freese
 */
@Repository
public class MyLdapDao implements BaseLdapNameAware
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MyLdapDao.class);

    /**
     * cn
     *
     * @author Thomas Freese
     */
    private static class GroupMemberDirContextMapper extends AbstractContextMapper<String[]>
    {
        /**
         * @see org.springframework.ldap.core.support.AbstractContextMapper#doMapFromContext(org.springframework.ldap.core.DirContextOperations)
         */
        @Override
        protected String[] doMapFromContext(final DirContextOperations ctx)
        {
            String[] member = ctx.getStringAttributes("uniqueMember");

            return member;
        }
    }

    /**
     * cn
     *
     * @author Thomas Freese
     */
    private static class PersonAttributeMapper implements AttributesMapper<String>
    {
        /**
         *
         */
        private final String attributeId;

        /**
         * Erstellt ein neues {@link PersonAttributeMapper} Object.
         *
         * @param attributeId String
         */
        public PersonAttributeMapper(final String attributeId)
        {
            super();

            this.attributeId = Objects.requireNonNull(attributeId, "attributeId required");
        }

        /**
         * @see org.springframework.ldap.core.AttributesMapper#mapFromAttributes(javax.naming.directory.Attributes)
         */
        @Override
        public String mapFromAttributes(final Attributes attributes) throws NamingException
        {
            Attribute attribute = attributes.get(this.attributeId);
            String value = null;

            if (attribute != null)
            {
                value = (String) attribute.get();
            }

            return value;
        }
    }

    /**
     * cn
     *
     * @author Thomas Freese
     */
    private static class PersonCommonNameContextMapper implements ContextMapper<String>
    {
        /**
         * @see org.springframework.ldap.core.ContextMapper#mapFromContext(java.lang.Object)
         */
        @Override
        public String mapFromContext(final Object ctx) throws NamingException
        {
            DirContextAdapter context = (DirContextAdapter) ctx;

            // context.getStringAttribute("entryDN");
            String cn = context.getStringAttribute("cn");

            return cn;
        }
    }

    /**
     * @return {@link Logger}
     */
    private static Logger getLogger()
    {
        return LOGGER;
    }

    /**
     *
     */
    private final LdapTemplate ldapTemplate;
    /**
     *
     */
    private LdapName baseLdapPath;

    /**
     * Erstellt ein neues {@link MyLdapDao} Object.
     *
     * @param contextSource {@link ContextSource}
     */
    public MyLdapDao(final ContextSource contextSource)
    {
        super();

        this.ldapTemplate = new LdapTemplate(Objects.requireNonNull(contextSource, "contextSource required"));
    }

    /**
     * uid=bob,ou=people,dc=springframework,dc=org
     *
     * @param userId String
     * @param password String
     * @param firstName String
     * @param lastName String
     */
    public void create(final String userId, final String password, final String firstName, final String lastName)
    {
        Name name = LdapNameBuilder.newInstance(getBaseLdapPath()).add("ou", "people").add("uid", userId).build();
        DirContextAdapter context = new DirContextAdapter(name);

        context.setAttributeValues("objectclass", new String[]
                {
                        "top", "person", "organizationalPerson", "inetOrgPerson"
                });
        context.setAttributeValue("cn", firstName + " " + lastName);
        context.setAttributeValue("sn", lastName);
        context.setAttributeValue("userPassword", digestSHA(password));

        getLdapTemplate().bind(context);
    }

    /**
     * @param userName String
     * @param password String
     *
     * @return String
     *
     * @throws Exception Falls was schief geht.
     */
    public String login(final String userName, final String password) throws Exception
    {
        // // "cn=" +
        // DirContext dirContext = getLdapTemplate().getContextSource().getContext(userName + "," + getBaseDN(), password);
        //
        // return (String) dirContext.getEnvironment().get("java.naming.security.principal");

        Name name = LdapNameBuilder.newInstance(getBaseLdapPath()).add(userName).build();
        DirContext ctx = null;

        try
        {
            ctx = getLdapTemplate().getContextSource().getContext(name.toString(), password);
            // Take care here - if a base was specified on the ContextSource
            // that needs to be removed from the user DN for the lookup to succeed.
            Object result = ctx.lookup(name);

            // return (String) ctx.getEnvironment().get("java.naming.security.principal");
            return new PersonCommonNameContextMapper().mapFromContext(result);
        }
        catch (Exception ex)
        {
            // Context creation failed - authentication did not succeed
            getLogger().error("Login failed", ex);

            return null;
        }
        finally
        {
            // It is imperative that the created DirContext instance is always closed
            org.springframework.ldap.support.LdapUtils.closeContext(ctx);
            // org.springframework.security.ldap.LdapUtils.closeContext(ctx);
        }
    }

    /**
     * @param userId String
     * @param password String
     * @param firstName String
     * @param lastName String
     */
    public void modify(final String userId, final String password, final String firstName, final String lastName)
    {
        Name name = LdapNameBuilder.newInstance(getBaseLdapPath()).add("ou", "people").add("uid", userId).build();
        DirContextOperations context = getLdapTemplate().lookupContext(name);

        context.setAttributeValues("objectclass", new String[]
                {
                        "top", "person", "organizationalPerson", "inetOrgPerson"
                });
        context.setAttributeValue("cn", firstName + " " + lastName);
        context.setAttributeValue("sn", lastName);
        context.setAttributeValue("userPassword", digestSHA(password));

        getLdapTemplate().modifyAttributes(context);
    }

    /**
     * uid = b*
     *
     * @param userId String
     *
     * @return {@link List}
     */
    public List<String> searchPeopleByUid(final String userId)
    {
        return searchPeopleByUid(userId, "cn");
    }

    /**
     * uid = b*
     *
     * @param userId String
     * @param attributeId String
     *
     * @return {@link List}
     */
    public List<String> searchPeopleByUid(final String userId, final String attributeId)
    {
        Name base = LdapNameBuilder.newInstance(getBaseLdapPath()).add("ou", "people").build();

        // return ldapTemplate.search(base, "uid=" + userId, (AttributesMapper<String>) attrs -> (String) attrs.get("cn").get());

        // @formatter:off
        LdapQuery query = LdapQueryBuilder.query()
                .searchScope(SearchScope.SUBTREE)
                .timeLimit(3 * 1000)
                .countLimit(10)
                .attributes(attributeId)
                .base(base)
                .where("objectclass").is("person")
                .and("uid").isPresent()
                .and("uid").like(userId)
                //.and("sn").not().is(lastName)
                ;
        // @formatter:on

        // return getLdapTemplate().search(query, (AttributesMapper<String>) attrs -> (String) attrs.get(attributeId).get());
        return getLdapTemplate().search(query, new PersonAttributeMapper(attributeId));
    }

    /**
     * cn = developers
     *
     * @param groupName String
     *
     * @return {@link List}
     */
    public List<String> searchPeopleInGroup(final String groupName)
    {
        Name base = LdapNameBuilder.newInstance(getBaseLdapPath()).add("ou", "groups").build();

        // return ldapTemplate.search(base, "uid=" + userId, (AttributesMapper<String>) attrs -> (String) attrs.get("cn").get());

        // @formatter:off
        LdapQuery query = LdapQueryBuilder.query()
                .attributes("uniqueMember")
                .base(base)
                .where("objectclass").is("groupOfUniqueNames")
                .and("cn").isPresent()
                .and("cn").is(groupName)
                ;
        // @formatter:on

        List<String[]> result = getLdapTemplate().search(query, new GroupMemberDirContextMapper());

        return result.stream().flatMap(Arrays::stream).sorted().toList();
    }

    /**
     * @see org.springframework.ldap.core.support.BaseLdapNameAware#setBaseLdapPath(javax.naming.ldap.LdapName)
     */
    @Override
    public void setBaseLdapPath(final LdapName baseLdapPath)
    {
        this.baseLdapPath = Objects.requireNonNull(baseLdapPath, "baseLdapPath required");
    }

    /**
     * @param password String
     *
     * @return String
     */
    private String digestSHA(final String password)
    {
        String base64 = null;

        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA");
            digest.update(password.getBytes(StandardCharsets.UTF_8));
            base64 = Base64.getEncoder().encodeToString(digest.digest());
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new RuntimeException(ex);
        }

        return "{SHA}" + base64;
    }

    /**
     * @return {@link LdapName}
     */
    private LdapName getBaseLdapPath()
    {
        return this.baseLdapPath;
    }

    /**
     * @return {@link LdapTemplate}
     */
    private LdapTemplate getLdapTemplate()
    {
        return this.ldapTemplate;
    }
}
