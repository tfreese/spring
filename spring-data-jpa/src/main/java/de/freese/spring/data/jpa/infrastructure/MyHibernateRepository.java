// Created: 16 Mai 2024
package de.freese.spring.data.jpa.infrastructure;

import java.util.Objects;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

/**
 * @author Thomas Freese
 */
@Repository
public class MyHibernateRepository {

    private final EntityManagerFactory entityManagerFactory;

    @Resource
    private SessionFactory sessionFactory;

    public MyHibernateRepository(final EntityManagerFactory entityManagerFactory) {
        super();

        this.entityManagerFactory = Objects.requireNonNull(entityManagerFactory, "entityManagerFactory required");
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    Session getSession() {
        return getSessionFactory().openSession();
    }
}
