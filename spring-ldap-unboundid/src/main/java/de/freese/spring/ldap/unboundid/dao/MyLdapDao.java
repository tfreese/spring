// Created: 18.02.2019
package de.freese.spring.ldap.unboundid.dao;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Repository;

/**
 * @author Thomas Freese
 */
@Repository
public class MyLdapDao
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
            String[] member = ctx.getStringAttributes("member");

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
     * Erstellt ein neues {@link MyLdapDao} Object.
     *
     * @param contextSource {@link ContextSource}
     * @param baseDn String
     */
    public MyLdapDao(final ContextSource contextSource, @Value("${spring.ldap.base-dn}") String baseDn)
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
        Name name = LdapNameBuilder.newInstance().add("ou", "people").add("uid", userId).build();
        DirContextAdapter context = new DirContextAdapter(name);

        context.setAttributeValues("objectclass", new String[]
                {
                        "top", "person", "organizationalPerson", "inetOrgPerson"
                });
        context.setAttributeValue("cn", firstName + " " + lastName);
        context.setAttributeValue("sn", lastName);
        context.setAttributeValue("userPassword", password);

        getLdapTemplate().bind(context);
    }

    /**
     * @param userId String
     * @param password String
     * @param firstName String
     * @param lastName String
     */
    public void modify(final String userId, final String password, final String firstName, final String lastName)
    {
        Name name = LdapNameBuilder.newInstance().add("ou", "people").add("uid", userId).build();
        DirContextOperations context = getLdapTemplate().lookupContext(name);

        context.setAttributeValues("objectclass", new String[]
                {
                        "top", "person", "organizationalPerson", "inetOrgPerson"
                });
        context.setAttributeValue("cn", firstName + " " + lastName);
        context.setAttributeValue("sn", lastName);
        context.setAttributeValue("userPassword", password);

        getLdapTemplate().modifyAttributes(context);
    }

    /**
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
        Name base = LdapNameBuilder.newInstance().add("ou", "people").build();

        // @formatter:off
        LdapQuery query = LdapQueryBuilder.query()
                .base(base)
                .searchScope(SearchScope.SUBTREE)
                .timeLimit(3 * 1000)
                .countLimit(10)
                .attributes(attributeId)
                .where("objectclass").is("person")
                .and("uid").isPresent()
                .and("uid").like(userId)
                //.and("sn").not().is(lastName)
                ;
        // @formatter:on

        return getLdapTemplate().search(query, new PersonAttributeMapper(attributeId));
    }

    /**
     * cn = developers
     *
     * @param groupName String
     *
     * @return {@link List}
     */
    public List<String> searchGroup(final String groupName)
    {
        Name base = LdapNameBuilder.newInstance().add("ou", "groups").build();

        // @formatter:off
        LdapQuery query = LdapQueryBuilder.query()
                .base(base)
                .searchScope(SearchScope.SUBTREE)
                .timeLimit(3 * 1000)
                .countLimit(10)
                .attributes("member")
                .where("objectclass").is("groupOfNames")
                .and("cn").isPresent()
                .and("cn").is(groupName)
                ;
        // @formatter:on

        List<String[]> result = getLdapTemplate().search(query, new GroupMemberDirContextMapper());

        return result.stream().flatMap(Arrays::stream).sorted().toList();
    }

    /**
     * @return {@link LdapTemplate}
     */
    private LdapTemplate getLdapTemplate()
    {
        return this.ldapTemplate;
    }
}