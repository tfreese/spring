// Created: 16 Mai 2024
package de.freese.spring.data.jpa.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import de.freese.spring.data.jpa.infrastructure.MyHibernateRepository;

/**
 * Bootstrapping Hibernate.<br>
 * Doe not work with {@link JpaRepository}, use custom Repositories like {@link MyHibernateRepository}.
 *
 * @author Thomas Freese
 */
// @Configuration
public class HibernateConfig {
    @Bean
    Properties hibernateProperties() {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");

        return hibernateProperties;
    }

    @Bean
    PlatformTransactionManager hibernateTransactionManager(final SessionFactory sessionFactory) {
        final HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory);

        return transactionManager;
    }

    @Bean
    LocalSessionFactoryBean sessionFactory(final DataSource dataSource, final Properties hibernateProperties) {
        final LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan("de.freese.spring.data.jpa.domain");
        sessionFactory.setHibernateProperties(hibernateProperties);

        return sessionFactory;
    }
}
