package de.freese.spring.ldap.unboundid.dao;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.jspecify.annotations.NonNull;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Repository;

/**
 * @author Thomas Freese
 * @since 18.02.2019
 */
@Repository
public class MyLdapDao {
    private static final String OBJECT_CLASS = "objectclass";
    private static final String OU_PEOPLE = "people";
    private static final String PERSON = "person";

    /**
     * cn
     *
     * @author Thomas Freese
     */
    static final class PersonCommonNameContextMapper implements ContextMapper<@NonNull String> {
        @Override
        public @NonNull String mapFromContext(final @NonNull Object ctx) {
            final DirContextAdapter context = (DirContextAdapter) ctx;

            // return context.getStringAttribute("entryDN");
            return Objects.requireNonNull(context.getStringAttribute("cn"), "cn attribute required");
        }
    }

    /**
     * cn
     *
     * @author Thomas Freese
     */
    private static final class GroupMemberDirContextMapper implements ContextMapper<String @NonNull []> {
        @Override
        public String @NonNull [] mapFromContext(final @NonNull Object ctx) {
            final DirContextOperations context = (DirContextOperations) ctx;

            return Objects.requireNonNull(context.getStringAttributes("member"), "member attribute required");
        }
    }

    /**
     * cn
     *
     * @author Thomas Freese
     */
    private record PersonAttributeMapper(String attributeId) implements AttributesMapper<@NonNull String> {
        private PersonAttributeMapper(final String attributeId) {
            this.attributeId = Objects.requireNonNull(attributeId, "attributeId required");
        }

        @Override
        public @NonNull String mapFromAttributes(final @NonNull Attributes attributes) throws NamingException {
            final Attribute attribute = Objects.requireNonNull(attributes.get(attributeId), "attribute required: " + attributeId);

            return Objects.requireNonNull((String) attribute.get(), "attribute value required: " + attributeId);
        }
    }

    private final LdapTemplate ldapTemplate;

    /**
     * final @Value("${spring.ldap.base-dn}") String baseDn
     */
    public MyLdapDao(final ContextSource contextSource) {
        super();

        ldapTemplate = new LdapTemplate(Objects.requireNonNull(contextSource, "contextSource required"));
    }

    /**
     * uid=bob,ou=people,dc=springframework,dc=org
     */
    public void create(final String userId, final String password, final String firstName, final String lastName) {
        final Name name = LdapNameBuilder.newInstance().add("ou", OU_PEOPLE).add("uid", userId).build();
        final DirContextAdapter context = new DirContextAdapter(name);

        context.setAttributeValues(OBJECT_CLASS, new String[]{"top", PERSON, "organizationalPerson", "inetOrgPerson"});
        context.setAttributeValue("cn", firstName + " " + lastName);
        context.setAttributeValue("sn", lastName);
        context.setAttributeValue("userPassword", password);

        getLdapTemplate().bind(context);
    }

    public void modify(final String userId, final String password, final String firstName, final String lastName) {
        final Name name = LdapNameBuilder.newInstance().add("ou", OU_PEOPLE).add("uid", userId).build();
        final DirContextOperations context = getLdapTemplate().lookupContext(name);

        context.setAttributeValues(OBJECT_CLASS, new String[]{"top", PERSON, "organizationalPerson", "inetOrgPerson"});
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
                .where(OBJECT_CLASS).is("groupOfNames")
                .and("cn").isPresent()
                .and("cn").is(groupName);

        final List<String[]> result = getLdapTemplate().search(query, new GroupMemberDirContextMapper());

        return result.stream().flatMap(Arrays::stream).sorted().toList();
    }

    /**
     * uid = b*
     */
    public List<String> searchPeopleByUid(final String userId, final String attributeId) {
        final Name base = LdapNameBuilder.newInstance().add("ou", OU_PEOPLE).build();

        final LdapQuery query = LdapQueryBuilder.query()
                .base(base)
                .searchScope(SearchScope.SUBTREE)
                .timeLimit(3 * 1000)
                .countLimit(10)
                .attributes(attributeId)
                .where(OBJECT_CLASS).is(PERSON)
                .and("uid").isPresent()
                .and("uid").like(userId)
                //.and("sn").not().is(lastName)
                ;

        return "cn".equals(attributeId)
                ? getLdapTemplate().search(query, new PersonCommonNameContextMapper())
                : getLdapTemplate().search(query, new PersonAttributeMapper(attributeId));
    }

    public List<String> searchPeopleByUid(final String userId) {
        return searchPeopleByUid(userId, "cn");
    }

    private LdapTemplate getLdapTemplate() {
        return ldapTemplate;
    }
}