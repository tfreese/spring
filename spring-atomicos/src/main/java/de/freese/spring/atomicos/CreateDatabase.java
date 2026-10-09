package de.freese.spring.atomicos;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Objects;

import javax.sql.DataSource;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 * @since 28.04.2022
 */
@Component
@Order(1)
public class CreateDatabase implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateDatabase.class);

    private final DataSource dataSourceAddress;
    private final DataSource dataSourcePerson;

    public CreateDatabase(final DataSource dataSourcePerson, final DataSource dataSourceAddress) {
        super();

        this.dataSourcePerson = Objects.requireNonNull(dataSourcePerson, "dataSourcePerson is required");
        this.dataSourceAddress = Objects.requireNonNull(dataSourceAddress, "dataSourceAddress is required");
    }

    @Override
    public void run(final @NonNull ApplicationArguments args) throws Exception {
        LOGGER.info("create person database");

        try (Connection connection = dataSourcePerson.getConnection();
             Statement statement = connection.createStatement()) {
            final String sql = """
                    create table PERSON (
                        ID bigint not null primary key,
                        NAME varchar(20) not null
                    )
                    """;

            statement.execute(sql);
        }

        LOGGER.info("create address database");

        try (Connection connection = dataSourceAddress.getConnection();
             Statement statement = connection.createStatement()) {
            final String sql = """
                    create table ADDRESS (
                        PERSON_ID bigint not null primary key,
                        CITY varchar(20) not null
                    )
                    """;

            statement.execute(sql);
        }

        LOGGER.info("databases created");
    }
}
