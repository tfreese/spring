// Created: 28.04.2022
package de.freese.spring.atomicos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Thomas Freese
 */
@Service
public class TxService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TxService.class);

    @Resource
    private DataSource dataSourceAddress;
    @Resource
    private DataSource dataSourcePerson;

    @Transactional(rollbackFor = Exception.class)
    public void insertData(final String personName, final String city) throws Exception {
        final long id = System.nanoTime();

        final String sqlPerson = """
                insert into PERSON
                    (ID, NAME)
                    values
                    (?, ?)
                """;
        try (Connection connection = this.dataSourcePerson.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlPerson)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setString(2, personName);

            preparedStatement.executeUpdate();
        }

        final String sqlAddress = """
                insert into ADDRESS
                    (PERSON_ID, CITY)
                    values
                    (?, ?)
                """;

        try (Connection connection = this.dataSourceAddress.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlAddress)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setString(2, city);

            preparedStatement.executeUpdate();
        }
    }

    public void selectAll() throws Exception {
        final Map<Long, String> personIdToCity = new HashMap<>();

        try (Connection connectionAddress = this.dataSourceAddress.getConnection();
             PreparedStatement preparedStatementAddress = connectionAddress.prepareStatement("select * from ADDRESS");
             ResultSet resultSetAddress = preparedStatementAddress.executeQuery()) {
            while (resultSetAddress.next()) {
                personIdToCity.put(resultSetAddress.getLong("PERSON_ID"), resultSetAddress.getString("CITY"));
            }
        }

        try (Connection connectionPerson = this.dataSourcePerson.getConnection();
             Statement statementPerson = connectionPerson.createStatement();
             ResultSet resultSetPerson = statementPerson.executeQuery("select * from PERSON")) {
            while (resultSetPerson.next()) {
                final long personId = resultSetPerson.getLong("ID");
                final String personName = resultSetPerson.getString("NAME");
                final String city = personIdToCity.getOrDefault(personId, null);

                LOGGER.info("{} - {} - {}", personId, personName, city);
            }
        }
    }
}
