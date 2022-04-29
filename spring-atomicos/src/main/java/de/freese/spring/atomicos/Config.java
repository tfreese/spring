// Created: 28.04.2022
package de.freese.spring.atomicos;

import java.util.Properties;

import javax.transaction.SystemException;

import com.atomikos.icatch.jta.UserTransactionManager;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * <a href="https://www.baeldung.com/java-atomikos">https://www.baeldung.com/java-atomikos</a><br>
 * <a href="https://github.com/eugenp/tutorials/tree/master/atomikos">https://github.com/eugenp/tutorials/tree/master/atomikos</a>
 *
 * @author Thomas Freese
 */
@Configuration
@EnableTransactionManagement
public class Config
{
    /**
     * @return AtomikosDataSourceBean
     */
    @Bean(initMethod = "init", destroyMethod = "close")
    public AtomikosDataSourceBean dataSourceAddress()
    {
        Properties xaProperties = new Properties();
        xaProperties.put("url", "jdbc:h2:mem:address;DB_CLOSE_DELAY=-1");
        xaProperties.put("user", "sa");
        xaProperties.put("password", "");

        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setXaDataSourceClassName(DatabaseDriver.H2.getXaDataSourceClassName());
        dataSource.setXaProperties(xaProperties);
        dataSource.setUniqueResourceName("address");
        dataSource.setLocalTransactionMode(true);
        dataSource.setMinPoolSize(2);
        dataSource.setMaxPoolSize(4);

        return dataSource;
    }

    /**
     * @return AtomikosDataSourceBean
     */
    @Bean(initMethod = "init", destroyMethod = "close")
    public AtomikosDataSourceBean dataSourcePerson()
    {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setUrl("jdbc:h2:mem:person;DB_CLOSE_DELAY=-1");
        h2DataSource.setUser("sa");
        h2DataSource.setUser("");

        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setXaDataSource(h2DataSource);
        dataSource.setUniqueResourceName("person");
        dataSource.setLocalTransactionMode(true);
        dataSource.setMinPoolSize(2);
        dataSource.setMaxPoolSize(4);

        return dataSource;
    }

    /**
     * @param userTransactionManager {@link com.atomikos.icatch.jta.UserTransactionManager}
     *
     * @return JtaTransactionManager
     *
     * @throws SystemException Falls was schiefgeht.
     */
    @Bean
    public JtaTransactionManager jtaTransactionManager(UserTransactionManager userTransactionManager) throws SystemException
    {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(userTransactionManager);
        jtaTransactionManager.setUserTransaction(userTransactionManager);

        return jtaTransactionManager;
    }

    /**
     * @return UserTransactionManager
     *
     * @throws SystemException Falls was schiefgeht.
     */
    @Bean(initMethod = "init", destroyMethod = "close")
    public UserTransactionManager userTransactionManager() throws SystemException
    {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setTransactionTimeout(300);
        userTransactionManager.setForceShutdown(true);

        return userTransactionManager;
    }
}

// Für Hibernate, momentan nur für Version 4.x verfügbar.
//
//<dependency>
//    <groupId>com.atomikos</groupId>
//    <artifactId>transactions-hibernate4</artifactId>
//    <version>5.0.9</version>
//</dependency>
//
//Integrating with Hibernate 5.2 and higher
//
//As of these hibernate releases, connections are closed differently: connection release mode has been replaced by a new property: hibernate.connection.handling_mode.
//Its value for JTA transaction should be set to DELAYED_ACQUISITION_AND_RELEASE_AFTER_STATEMENT:
//
//Otherwise, you will exhaust the connection pool.
//
//hibernate.connection.handling_mode = DELAYED_ACQUISITION_AND_RELEASE_AFTER_STATEMENT
//
//@Configuration
//@EnableJpaRepositories(basePackages = "de.freese.spring.atomicos.person", entityManagerFactoryRef = "entityManagerPerson", transactionManagerRef = "transactionManager")
//public class PersonConfig {
//    @Bean(initMethod = "init", destroyMethod = "close")
//    public AtomikosDataSourceBean dataSourcePerson() {
//        ...
//    }
//
//    @Bean
//    public EntityManagerFactory entityManagerPerson(DataSource dataSourcePerson) {
//        Properties jpaProperties = new Properties();
//        jpaProperties.put("hibernate.show_sql", "true");
//        jpaProperties.put("hibernate.format_sql", "true");
//        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
//        jpaProperties.put("hibernate.current_session_context_class", "jta");
//        jpaProperties.put("javax.persistence.transactionType", "jta");
//        jpaProperties.put("hibernate.transaction.manager_lookup_class", "com.atomikos.icatch.jta.hibernate3.TransactionManagerLookup");
//        jpaProperties.put("hibernate.hbm2ddl.auto", "create-drop");
//
//        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
//        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
//        factory.setPackagesToScan("de.freese.spring.atomicos.person");
//        factory.setDataSource(dataSourcePerson);
//        factory.setJpaProperties(jpaProperties);
//        factory.afterPropertiesSet();
//
//        return factory.getObject();
//    }
//}
