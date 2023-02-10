// Created: 28.04.2022
package de.freese.spring.atomicos;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
@Order(1)
public class CreateDatabase implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateDatabase.class);

    @Resource
    private DataSource dataSourceAddress;

    @Resource
    private DataSource dataSourcePerson;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOGGER.info("create person database");

        try (Connection connection = this.dataSourcePerson.getConnection(); Statement statement = connection.createStatement()) {
            String sql = """
                    create table PERSON (
                        ID bigint  not null primary key,
                        NAME varchar(20) not null
                    )
                    """;

            statement.execute(sql);
        }

        LOGGER.info("create address database");

        try (Connection connection = this.dataSourceAddress.getConnection(); Statement statement = connection.createStatement()) {
            String sql = """
                    create table ADDRESS (
                        PERSON_ID bigint  not null primary key,
                        CITY varchar(20) not null
                    )
                    """;

            statement.execute(sql);
        }

        LOGGER.info("databases created");
    }
}
