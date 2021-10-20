// Created: 19.10.2019
package de.freese.spring.autoconfigure.h2server;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.SocketUtils;

import de.freese.spring.autoconfigure.TestAutoConfiguration;

/**
 * @author Thomas Freese
 */
@SpringBootTest(classes = TestH2Config.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
class TestH2ServerAutoConfiguration implements TestAutoConfiguration
{
    static
    {
        int port = SocketUtils.findAvailableTcpPort();

        // Damit die Placeholder in Properties funktionieren: ${hsqldb.server.port}
        System.setProperty("portH2", Integer.toString(port));
    }

    /**
    *
    */
    @Resource
    @Qualifier("dataSourceH2File")
    private DataSource dataSourceFile;

    /**
    *
    */
    @Resource
    @Qualifier("dataSourceH2Memory")
    private DataSource dataSourceMemory;

    /**
     * @see de.freese.spring.autoconfigure.TestAutoConfiguration#getDataSourceFile()
     */
    @Override
    public DataSource getDataSourceFile()
    {
        return this.dataSourceFile;
    }

    /**
     * @see de.freese.spring.autoconfigure.TestAutoConfiguration#getDataSourceMemory()
     */
    @Override
    public DataSource getDataSourceMemory()
    {
        return this.dataSourceMemory;
    }
}
