/**
 * Created: 17.02.2019
 */

package de.freese.spring.ldap.unboundid.config;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.support.BaseLdapNameAware;
import org.springframework.ldap.core.support.BaseLdapPathBeanPostProcessor;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;

/**
 * @author Thomas Freese
 */
@Configuration
public class LdapClientConfig
{
    /**
    *
    */
    @Value("${spring.ldap.embedded.base-dn}")
    private String baseDN = null;

    /**
    *
    */
    @Resource
    private InMemoryDirectoryServer directoryServer = null;

    /**
     *
     */
    @Resource
    private Environment env = null;

    /**
     * Erstellt ein neues {@link LdapClientConfig} Object.
     */
    public LdapClientConfig()
    {
        super();
    }

    /**
     * @return {@link BaseLdapPathBeanPostProcessor}
     * @see BaseLdapNameAware
     */
    @Bean
    public BaseLdapPathBeanPostProcessor baseLdapPathBeanPostProcessor()
    {
        BaseLdapPathBeanPostProcessor postProcessor = new BaseLdapPathBeanPostProcessor();
        postProcessor.setBasePath(this.baseDN);

        return postProcessor;
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @PreDestroy
    private void exportLDIF() throws Exception
    {
        System.out.println("LdapClientConfig.exportLDIF()");

        this.directoryServer.exportToLDIF("export.ldif", true, true);

        // // Perform a search to find all users who are members of the people department.
        // // SearchRequest searchRequest = new SearchRequest("dc=springframework,dc=org", SearchScope.SUB, Filter.createEqualityFilter("ou", "people"));
        // SearchRequest searchRequest = new SearchRequest("ou=people,dc=springframework,dc=org", SearchScope.SUB, Filter.createPresenceFilter("uid"));
        // SearchResult searchResult = null;
        //
        // try
        // {
        // searchResult = this.directoryServer.getConnection().search(searchRequest);
        // }
        // catch (LDAPSearchException lse)
        // {
        // searchResult = lse.getSearchResult();
        // }
        //
        // // Write all of the matching entries to LDIF.
        // int entriesWritten = 0;
        //
        // try (LDIFWriter ldifWriter = new LDIFWriter("export.ldif"))
        // {
        // for (SearchResultEntry entry : searchResult.getSearchEntries())
        // {
        // ldifWriter.writeEntry(entry);
        // entriesWritten++;
        // }
        // }
        //
        // System.out.println("entries written: " + entriesWritten);
    }

    // /**
    // * @param ldapProperties {@link LdapProperties}
    // * @param embeddedLdapProperties {@link EmbeddedLdapProperties}
    // * @return {@link ContextSource}
    // */
    // @Bean
    // public ContextSource ldapContextSource(final LdapProperties ldapProperties, final EmbeddedLdapProperties embeddedLdapProperties)
    // {
    // LdapContextSource contextSource = new LdapContextSource();
    //
    // contextSource.setUrls(ldapProperties.determineUrls(this.env));
    // contextSource.setBase(embeddedLdapProperties.getBaseDn().get(0));
    //
    // Credential credential = embeddedLdapProperties.getCredential();
    //
    // if (StringUtils.hasText(credential.getUsername()) && StringUtils.hasText(credential.getPassword()))
    // {
    // contextSource.setUserDn(credential.getUsername());
    // contextSource.setPassword(credential.getPassword());
    // }
    //
    // return contextSource;
    // }

    // /**
    // * @param ldapContextSource {@link ContextSource}
    // * @return {@link LdapTemplate}
    // */
    // @Bean
    // public LdapTemplate ldapTemplate(final ContextSource ldapContextSource)
    // {
    // return new LdapTemplate(ldapContextSource);
    // }
}
