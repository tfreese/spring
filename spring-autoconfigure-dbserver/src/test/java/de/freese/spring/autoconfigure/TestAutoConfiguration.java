// Created: 20.10.2021
package de.freese.spring.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@SpringBootTest
@ActiveProfiles("test")
public interface TestAutoConfiguration
{
    /**
     * @param dataSource {@link DataSource}
     *
     * @throws SQLException Falls was schief geht.
     */
    default void createTable(final DataSource dataSource) throws SQLException
    {
        assertNotNull(dataSource);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement())
        {
            statement.execute("DROP TABLE IF EXISTS PERSON CASCADE");
            statement.execute("CREATE TABLE PERSON(ID BIGINT NOT NULL PRIMARY KEY, NAME VARCHAR(25) NOT NULL, VORNAME VARCHAR(25))");
        }
    }

    /**
     * @return {@link DataSource}
     */
    DataSource getDataSourceFile();

    /**
     * @return {@link DataSource}
     */
    DataSource getDataSourceMemory();

    /**
     * @param dataSource {@link DataSource}
     *
     * @throws SQLException Falls was schief geht.
     */
    default void insert(final DataSource dataSource) throws SQLException
    {
        assertNotNull(dataSource);

        try (Connection con = dataSource.getConnection();
             PreparedStatement stmt = con.prepareStatement("insert into PERSON (ID, NAME) values (?, ?)"))
        {
            stmt.setLong(1, 7);
            stmt.setString(2, "Test");

            stmt.execute();
            con.commit();
        }
    }

    /**
     * @param dataSource {@link DataSource}
     *
     * @throws SQLException Falls was schief geht.
     */
    default void select(final DataSource dataSource) throws SQLException
    {
        assertNotNull(dataSource);

        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("select * from PERSON"))
        {
            final boolean hasNext = rs.next();

            if (hasNext)
            {
                do
                {
                    assertEquals(7L, rs.getLong("ID"));
                    assertEquals("Test", rs.getString("NAME"));
                }
                while (rs.next());
            }
            else
            {
                assertTrue(false);
            }
        }
    }

    /**
    *
    */
    @Test
    default void testContextLoads()
    {
        assertTrue(true);
    }

    /**
     * @throws SQLException Falls was schief geht.
     */
    @Test
    // @Transactional("nameOfTransactionManager")
    default void testDataSourceFile() throws SQLException
    {
        createTable(getDataSourceFile());
        insert(getDataSourceFile());
        select(getDataSourceFile());
    }

    /**
     * @throws SQLException Falls was schief geht.
     */
    @Test
    // @Transactional("nameOfTransactionManager")
    default void testDataSourceMemory() throws SQLException
    {
        createTable(getDataSourceMemory());
        insert(getDataSourceMemory());
        select(getDataSourceMemory());
    }
}