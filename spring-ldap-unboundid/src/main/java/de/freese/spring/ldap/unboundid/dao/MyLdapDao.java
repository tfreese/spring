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
public class MyLdapDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyLdapDao.class);

    /**
     * cn
     *
     * @author Thomas Freese
     */
    static final class PersonCommonNameContextMapper implements ContextMapper<String> {
        @Override
        public String mapFromContext(final Object ctx) {
            final DirContextAdapter context = (DirContextAdapter) ctx;

            // return context.getStringAttribute("entryDN");
            return context.getStringAttribute("cn");
        }
    }

    /**
     * cn
     *
     * @author Thomas Freese
     */
    private static final class GroupMemberDirContextMapper extends AbstractContextMapper<String[]> {
        @Override
        protected String[] doMapFromContext(final DirContextOperations ctx) {
            return ctx.getStringAttributes("member");
        }
    }

    /**
     * cn
     *
     * @author Thomas Freese
     */
    private static final class PersonAttributeMapper implements AttributesMapper<String> {
        private final String attributeId;

        PersonAttributeMapper(final String attributeId) {
            super();

            this.attributeId = Objects.requireNonNull(attributeId, "attributeId required");
        }

        @Override
        public String mapFromAttributes(final Attributes attributes) throws NamingException {
            final Attribute attribute = attributes.get(this.attributeId);
            String value = null;

            if (attribute != null) {
                value = (String) attribute.get();
            }

            return value;
        }
    }

    static Logger getLogger() {
        return LOGGER;
    }

    private final LdapTemplate ldapTemplate;

    public MyLdapDao(final ContextSource contextSource, final @Value("${spring.ldap.base-dn}") String baseDn) {
        super();

        this.ldapTemplate = new LdapTemplate(Objects.requireNonNull(contextSource, "contextSource required"));
    }

    /**
     * uid=bob,ou=people,dc=springframework,dc=org
     */
    public void create(final String userId, final String password, final String firstName, final String lastName) {
        final Name name = LdapNameBuilder.newInstance().add("ou", "people").add("uid", userId).build();
        final DirContextAdapter context = new DirContextAdapter(name);

        context.setAttributeValues("objectclass", new String[]{"top", "person", "organizationalPerson", "inetOrgPerson"});
        context.setAttributeValue("cn", firstName + " " + lastName);
        context.setAttributeValue("sn", lastName);
        context.setAttributeValue("userPassword", password);

        getLdapTemplate().bind(context);
    }

    public void modify(final String userId, final String password, final String firstName, final String lastName) {
        final Name name = LdapNameBuilder.newInstance().add("ou", "people").add("uid", userId).build();
        final DirContextOperations context = getLdapTemplate().lookupContext(name);

        context.setAttributeValues("objectclass", new String[]{"top", "person", "organizationalPerson", "inetOrgPerson"});
        context.setAttributeValue("cn", firstName + " " + lastName);
        context.setAttributeValue("sn", lastName);
        context.setAttributeValue("userPassword", password);

        getLdapTemplate().modifyAttributes(context);
    }

    /**
     * cn = developers
     */
    public List<String> searchGroup(final String groupName) {
        final Name base = LdapNameBuilder.newInstance().add("ou", "groups").build();

        final LdapQuery query = LdapQueryBuilder.query()
                .base(base)
                .searchScope(SearchScope.SUBTREE)
                .timeLimit(3 * 1000)
                .countLimit(10)
                .attributes("member")
                .where("objectclass").is("groupOfNames")
                .and("cn").isPresent()
                .and("cn").is(groupName);

        final List<String[]> result = getLdapTemplate().search(query, new GroupMemberDirContextMapper());

        return result.stream().flatMap(Arrays::stream).sorted().toList();
    }

    /**
     * uid = b*
     */
    public List<String> searchPeopleByUid(final String userId, final String attributeId) {
        final Name base = LdapNameBuilder.newInstance().add("ou", "people").build();

        final LdapQuery query = LdapQueryBuilder.query()
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

        return getLdapTemplate().search(query, new PersonAttributeMapper(attributeId));
    }

    public List<String> searchPeopleByUid(final String userId) {
        return searchPeopleByUid(userId, "cn");
    }

    private LdapTemplate getLdapTemplate() {
        return this.ldapTemplate;
    }
}
