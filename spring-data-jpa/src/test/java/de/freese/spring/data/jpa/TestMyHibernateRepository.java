// Created: 11.10.2025
package de.freese.spring.data.jpa;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import de.freese.spring.data.jpa.infrastructure.MyHibernateRepository;

/**
 * @author Thomas Freese
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TestMyHibernateRepository {
    @Resource
    private MyHibernateRepository myHibernateRepository;

    @Test
    void testMyHibernateRepository() {
        assertNotNull(myHibernateRepository);
        assertNotNull(myHibernateRepository.getEntityManagerFactory());
        assertNotNull(myHibernateRepository.getSessionFactory());
    }
}
