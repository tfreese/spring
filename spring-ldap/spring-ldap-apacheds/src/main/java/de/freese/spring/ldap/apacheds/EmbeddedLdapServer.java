/**
 * Created: 20.02.2019
 */
package de.freese.spring.ldap.apacheds;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.schema.registries.DefaultSchema;
import org.apache.directory.api.ldap.model.schema.registries.Schema;
import org.apache.directory.api.ldap.schema.loader.JarLdifSchemaLoader;
import org.apache.directory.api.ldap.schema.loader.LdifSchemaLoader;
import org.apache.directory.api.util.FileUtils;
import org.apache.directory.server.core.api.CoreSession;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.factory.DefaultDirectoryServiceFactory;
import org.apache.directory.server.core.factory.JdbmPartitionFactory;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.store.LdifFileLoader;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.server.xdbm.Index;
import org.apache.directory.server.xdbm.IndexNotFoundException;

/**
 * API References:<br>
 * http://directory.apache.org/apacheds/gen-docs/latest/apidocs/<br>
 * http://directory.apache.org/api/gen-docs/latest/apidocs/<br>
 * https://github.com/ztarbug/apacheds-embedded/blob/master/src/main/java/de/starwit/auth/apacheds/DirectoryRunner.java<br>
 * Call init() to start the server and destroy() to shut it down.
 */
public class EmbeddedLdapServer
{
    /**
     *
     */
    private static final List<String> ATTR_NAMES_TO_INDEX = new ArrayList<>(Arrays.asList("uid"));

    /**
     *
     */
    private static final int BASE_CACHE_SIZE = 1000;

    /**
     *
     */
    private static final String BASE_DOMAIN = "org";

    /**
     *
     */
    private static final String BASE_PARTITION_NAME = "mydomain";

    /**
     *
     */
    private static final String BASE_STRUCTURE = "dc=" + BASE_PARTITION_NAME + ",dc=" + BASE_DOMAIN;

    /**
     *
     */
    private static final int LDAP_SERVER_PORT = 10389;

    /**
     * @param path {@link File}
     * @throws IOException Falls was schief geht.
     */
    private static void deleteDirectory(final File path) throws IOException
    {
        FileUtils.deleteDirectory(path);
    }

    /**
     * Main class. We just do a lookup on the server to check that it's available.
     *
     * @param args Not used.
     * @throws Exception Falls was schief geht.
     */
    public static void main(final String[] args) throws Exception
    {
        EmbeddedLdapServer ads = null;

        try
        {
            // Create the server
            ads = new EmbeddedLdapServer();
            ads.init();
            ads.setDeleteInstanceDirectoryOnShutdown(false);

            // ads.addSchemaFromPath(new File("/home/tommy/git/spring/spring-ldap"), "evolutionperson.schema");

            LdifFileLoader ldifLoader = null;

            ldifLoader = new LdifFileLoader(ads.getDirectoryService().getAdminSession(), "/usr/share/evolution-data-server/evolutionperson.schema");
            ldifLoader.execute();

            ads.addPartition("people", "dc=springframework,dc=org");

            ldifLoader = new LdifFileLoader(ads.getDirectoryService().getAdminSession(), "../spring-ldap-unboundid/src/main/resources/test-server.ldif");
            ldifLoader.execute();

            // Read an entry
            Entry result = ads.getDirectoryService().getAdminSession().lookup(ads.getDirectoryService().getDnFactory().create("dc=mydomain,dc=org"));
            System.out.println("Found entry : " + result);

            result = ads.getDirectoryService().getAdminSession()
                    .lookup(ads.getDirectoryService().getDnFactory().create("uid=tommy,ou=people,dc=springframework,dc=org"));
            System.out.println("Found entry : " + result);
        }
        catch (Exception ex)
        {
            // Ok, we have something wrong going on ...
            ex.printStackTrace();

            System.exit(-1);
        }
        finally
        {
            if (ads != null)
            {
                ads.destroy();
            }
        }
    }

    /**
     *
     */
    private JdbmPartition basePartition;

    /**
     *
     */
    private boolean deleteInstanceDirectoryOnShutdown = true;

    /**
     *
     */
    private boolean deleteInstanceDirectoryOnStartup = true;

    /**
     *
     */
    private DirectoryService directoryService;

    /**
     *
     */
    private LdapServer ldapServer;

    /**
     * @param name String; mydomain
     * @param baseStructure String; dc=mydomain,dc=org
     * @return {@link JdbmPartition}
     * @throws Exception Falls was schief geht.
     */
    public JdbmPartition addPartition(final String name, final String baseStructure) throws Exception
    {
        // JdbmPartition partition = new JdbmPartition(getDirectoryService().getSchemaManager(),getDirectoryService().getDnFactory());
        // partition.setId( partitionId );
        // partition.setPartitionDir( new File(getPartitionsDirectory(), partitionId ) );
        // partition.setSuffixDn(getDirectoryService().getDnFactory().create( partitionDn ));
        // getDirectoryService().addPartition( partition );

        JdbmPartitionFactory jdbmPartitionFactory = new JdbmPartitionFactory();
        JdbmPartition partition = jdbmPartitionFactory.createPartition(getDirectoryService().getSchemaManager(), getDirectoryService().getDnFactory(), name,
                baseStructure, getBaseCacheSize(), new File(getPartitionsDirectory(), name));

        for (String attrName : getAttrNamesToIndex())
        {
            partition.addIndex(createIndexObjectForAttr(attrName));
        }

        getDirectoryService().addPartition(partition);

        return partition;
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    protected void addPartitionBase() throws Exception
    {
        JdbmPartitionFactory jdbmPartitionFactory = new JdbmPartitionFactory();
        JdbmPartition partition = jdbmPartitionFactory.createPartition(getDirectoryService().getSchemaManager(), getDirectoryService().getDnFactory(),
                getBasePartitionName(), getBaseStructure(), getBaseCacheSize(), getBasePartitionPath());

        setBasePartition(partition);

        addSchemaExtensions();

        createBaseIndices();

        getDirectoryService().addPartition(getBasePartition());
    }

    /**
     * @throws LdapException Falls was schief geht.
     * @throws IOException Falls was schief geht.
     */
    protected void addSchemaExtensions() throws LdapException, IOException
    {
        // override to add custom attributes to the schema
    }

    /**
     * Add additional schemas to the directory server. This uses JarLdifSchemaLoader, which will search for the "ou=schema" directory within "/schema" on the
     * classpath. If packaging the schema as part of a jar using Gradle or Maven, you'd probably want to put your "ou=schema" directory in
     * src/main/resources/schema.
     * <p/>
     * It's also required that a META-INF/apacheds-schema.index be present in your classpath that lists each LDIF file in your schema directory.
     *
     * @param schemaName The name of the schema
     * @return true if the schemas have been loaded and the registries is consistent
     * @throws Exception Falls was schief geht.
     */
    public boolean addSchemaFromClasspath(final String schemaName) throws Exception
    {
        // To debug if your apacheds-schema.index isn't found:
        // Enumeration<URL> indexes = getClass().getClassLoader().getResources("META-INF/apacheds-schema.index");
        JarLdifSchemaLoader schemaLoader = new JarLdifSchemaLoader();
        Schema schema = schemaLoader.getSchema(schemaName);

        return (schema != null) && getDirectoryService().getSchemaManager().load(schema);
    }

    /**
     * Add additional schemas to the directory server. This takes a path to the schema directory and uses the LdifSchemaLoader.
     *
     * @param schemaLocation The path to the directory containing the "ou=schema" directory for an additional schema
     * @param schemaName The name of the schema
     * @return true if the schemas have been loaded and the registries is consistent
     * @throws Exception Falls was schief geht.
     */
    public boolean addSchemaFromPath(final File schemaLocation, final String schemaName) throws Exception
    {
        LdifSchemaLoader schemaLoader = new LdifSchemaLoader(schemaLocation);
        DefaultSchema schema = new DefaultSchema(schemaLoader, schemaName);

        return getDirectoryService().getSchemaManager().load(schema);
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    protected void createBaseIndices() throws Exception
    {
        //
        // Default indices, that can be seen with getSystemIndexMap() and
        // getUserIndexMap(), are minimal. There are no user indices by
        // default and the default system indices are:
        //
        // apacheOneAlias, entryCSN, apacheSubAlias, apacheAlias,
        // objectClass, apachePresence, apacheRdn, administrativeRole
        //
        for (String attrName : getAttrNamesToIndex())
        {
            getBasePartition().addIndex(createIndexObjectForAttr(attrName));
        }
    }

    /**
     * @param attrName String
     * @return {@link JdbmIndex}
     * @throws LdapException Falls was schief geht.
     */
    protected JdbmIndex<?> createIndexObjectForAttr(final String attrName) throws LdapException
    {
        return createIndexObjectForAttr(attrName, false);
    }

    /**
     * @param attrName String
     * @param withReverse boolean
     * @return {@link JdbmIndex}
     * @throws LdapException Falls was schief geht.
     */
    protected JdbmIndex<?> createIndexObjectForAttr(final String attrName, final boolean withReverse) throws LdapException
    {
        String oid = getOidByAttributeName(attrName);

        if (oid == null)
        {
            throw new RuntimeException("OID could not be found for attr " + attrName);
        }

        return new JdbmIndex<>(oid, withReverse);
    }

    /**
     * @throws LdapException Falls was schief geht.
     */
    protected void createRootEntry() throws LdapException
    {
        Entry entry = getDirectoryService().newEntry(getDirectoryService().getDnFactory().create(getBaseStructure()));
        entry.add("objectClass", "top", "domain", "extensibleObject");
        entry.add("dc", getBasePartitionName());
        CoreSession session = getDirectoryService().getAdminSession();

        try
        {
            session.add(entry);
        }
        finally
        {
            session.unbind();
        }
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    public void destroy() throws Exception
    {
        File instanceDirectory = getDirectoryService().getInstanceLayout().getInstanceDirectory();

        getLdapServer().stop();
        getDirectoryService().shutdown();
        setLdapServer(null);
        setDirectoryService(null);

        if (isDeleteInstanceDirectoryOnShutdown())
        {
            deleteDirectory(instanceDirectory);
        }
    }

    /**
     * @return {@link List}
     */
    public List<String> getAttrNamesToIndex()
    {
        return ATTR_NAMES_TO_INDEX;
    }

    /**
     * @return int
     */
    public int getBaseCacheSize()
    {
        return BASE_CACHE_SIZE;
    }

    /**
     * @return {@link JdbmPartition}
     */
    public JdbmPartition getBasePartition()
    {
        return this.basePartition;
    }

    /**
     * @return String
     */
    public String getBasePartitionName()
    {
        return BASE_PARTITION_NAME;
    }

    /**
     * @return {@link File}
     */
    public File getBasePartitionPath()
    {
        return new File(getPartitionsDirectory(), getBasePartitionName());
    }

    /**
     * @return String
     */
    public String getBaseStructure()
    {
        return BASE_STRUCTURE;
    }

    /**
     * @return {@link DirectoryService}
     */
    public DirectoryService getDirectoryService()
    {
        return this.directoryService;
    }

    /**
     * @return String
     */
    public String getDirectoryServiceName()
    {
        return getBasePartitionName() + "DirectoryService";
    }

    /**
     * Used at init time to clear out the likely instance directory before anything is created.
     *
     * @return {@link File}
     */
    public File getGuessedInstanceDirectory()
    {
        // See source code for DefaultDirectoryServiceFactory
        // buildInstanceDirectory. ApacheDS looks at the workingDirectory
        // system property first and then defers to the java.io.tmpdir
        // system property.
        final String property = System.getProperty("workingDirectory");

        return new File(property != null ? property : System.getProperty("java.io.tmpdir") + File.separator + "server-work-" + getDirectoryServiceName());
    }

    /**
     * @return {@link LdapServer}
     */
    public LdapServer getLdapServer()
    {
        return this.ldapServer;
    }

    /**
     * @return int
     */
    public int getLdapServerPort()
    {
        return LDAP_SERVER_PORT;
    }

    /**
     * @param attrName String
     * @return String
     * @throws LdapException Falls was schief geht.
     */
    public String getOidByAttributeName(final String attrName) throws LdapException
    {
        return getDirectoryService().getSchemaManager().getAttributeTypeRegistry().getOidByName(attrName);
    }

    /**
     * @return {@link File}
     */
    public File getPartitionsDirectory()
    {
        return getDirectoryService().getInstanceLayout().getPartitionsDirectory();
    }

    /**
     * @return A map where the key is the attribute name the value is the oid.
     * @throws IndexNotFoundException Falls was schief geht.
     */
    public Map<String, String> getSystemIndexMap() throws IndexNotFoundException
    {
        Map<String, String> result = new LinkedHashMap<>();
        Iterator<String> it = getBasePartition().getSystemIndices();

        while (it.hasNext())
        {
            String oid = it.next();
            Index<?, String> index = getBasePartition().getSystemIndex(getDirectoryService().getSchemaManager().getAttributeType(oid));
            result.put(index.getAttribute().getName(), index.getAttributeId());
        }

        return result;
    }

    /**
     * @return A map where the key is the attribute name the value is the oid.
     * @throws IndexNotFoundException Falls was schief geht.
     */
    public Map<String, String> getUserIndexMap() throws IndexNotFoundException
    {
        Map<String, String> result = new LinkedHashMap<>();
        Iterator<String> it = getBasePartition().getUserIndices();

        while (it.hasNext())
        {
            String oid = it.next();
            Index<?, String> index = getBasePartition().getUserIndex(getDirectoryService().getSchemaManager().getAttributeType(oid));
            result.put(index.getAttribute().getName(), index.getAttributeId());
        }

        return result;
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    public void init() throws Exception
    {
        if (getDirectoryService() == null)
        {
            if (isDeleteInstanceDirectoryOnStartup())
            {
                deleteDirectory(getGuessedInstanceDirectory());
            }

            DefaultDirectoryServiceFactory serviceFactory = new DefaultDirectoryServiceFactory();
            serviceFactory.init(getDirectoryServiceName());
            setDirectoryService(serviceFactory.getDirectoryService());

            getDirectoryService().getChangeLog().setEnabled(false);
            getDirectoryService().setDenormalizeOpAttrsEnabled(true);
            getDirectoryService().setShutdownHookEnabled(true);
            // getDirectoryService().set;

            addPartitionBase();

            getDirectoryService().startup();

            createRootEntry();
        }

        if (getLdapServer() == null)
        {
            setLdapServer(new LdapServer());
            getLdapServer().setDirectoryService(getDirectoryService());
            getLdapServer().setTransports(new TcpTransport(getLdapServerPort()));
            getLdapServer().start();
        }

        // CacheService cacheService = new CacheService();
        // cacheService.initialize(getDirectoryService().getInstanceLayout());
        // getDirectoryService().setCacheService(cacheService);
    }

    /**
     * @return boolean
     */
    public boolean isDeleteInstanceDirectoryOnShutdown()
    {
        return this.deleteInstanceDirectoryOnShutdown;
    }

    /**
     * @return boolean
     */
    public boolean isDeleteInstanceDirectoryOnStartup()
    {
        return this.deleteInstanceDirectoryOnStartup;
    }

    /**
     * @param basePartition {@link JdbmPartition}
     */
    public void setBasePartition(final JdbmPartition basePartition)
    {
        this.basePartition = basePartition;
    }

    /**
     * @param deleteInstanceDirectoryOnShutdown boolean
     */
    public void setDeleteInstanceDirectoryOnShutdown(final boolean deleteInstanceDirectoryOnShutdown)
    {
        this.deleteInstanceDirectoryOnShutdown = deleteInstanceDirectoryOnShutdown;
    }

    /**
     * @param deleteInstanceDirectoryOnStartup boolean
     */
    public void setDeleteInstanceDirectoryOnStartup(final boolean deleteInstanceDirectoryOnStartup)
    {
        this.deleteInstanceDirectoryOnStartup = deleteInstanceDirectoryOnStartup;
    }

    /**
     * @param directoryService {@link DirectoryService}
     */
    public void setDirectoryService(final DirectoryService directoryService)
    {
        this.directoryService = directoryService;
    }

    /**
     * @param ldapServer {@link LdapServer}
     */
    public void setLdapServer(final LdapServer ldapServer)
    {
        this.ldapServer = ldapServer;
    }
}
