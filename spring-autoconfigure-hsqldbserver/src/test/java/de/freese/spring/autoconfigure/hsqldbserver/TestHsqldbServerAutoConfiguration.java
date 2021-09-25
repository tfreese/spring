// Created: 19.10.2019
package de.freese.spring.autoconfigure.hsqldbserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.SocketUtils;

/**
 * @author Thomas Freese
 */
@SpringBootTest(classes = TestApplication.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
class TestHsqldbServerAutoConfiguration
{
    static
    {
        int port = SocketUtils.findAvailableTcpPort();

        // Damit die Placeholder in Properties funktionieren: ${hsqldb.server.port}
        System.setProperty("port", Integer.toString(port));
    }

    /**
     * @throws Exception Falls was schief geht.
     */
    @BeforeAll
    static void beforeAll() throws Exception
    {
        // Empty
    }

    /**
    *
    */
    @Resource
    @Qualifier("dataSourceFile")
    private DataSource dataSourceFile;

    /**
    *
    */
    @Resource
    @Qualifier("dataSourceMemory")
    private DataSource dataSourceMemory;

    /**
     * @param dataSource {@link DataSource}
     *
     * @throws SQLException Falls was schief geht.
     */
    private void createTable(final DataSource dataSource) throws SQLException
    {
        assertNotNull(dataSource);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement())
        {
            statement.execute("DROP TABLE IF EXISTS PERSON CASCADE");
            statement.execute("CREATE TABLE PERSON(ID BIGINT NOT NULL, NAME VARCHAR(25) NOT NULL, VORNAME VARCHAR(25), PRIMARY KEY (ID))");
        }
    }

    /**
     * @param dataSource {@link DataSource}
     *
     * @throws SQLException Falls was schief geht.
     */
    private void insert(final DataSource dataSource) throws SQLException
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
    private void select(final DataSource dataSource) throws SQLException
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
    void testContextLoads()
    {
        assertTrue(true);
    }

    /**
     * @throws SQLException Falls was schief geht.
     */
    @Test
    void testDataSourceFile() throws SQLException
    {
        createTable(this.dataSourceFile);
        insert(this.dataSourceFile);
        select(this.dataSourceFile);
    }

    /**
     * @throws SQLException Falls was schief geht.
     */
    @Test
    void testDataSourceMemory() throws SQLException
    {
        createTable(this.dataSourceMemory);
        insert(this.dataSourceMemory);
        select(this.dataSourceMemory);
    }
}
