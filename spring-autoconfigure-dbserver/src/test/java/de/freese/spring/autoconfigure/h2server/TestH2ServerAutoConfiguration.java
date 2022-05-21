// Created: 19.10.2019
package de.freese.spring.autoconfigure.h2server;

import javax.annotation.Resource;
import javax.sql.DataSource;

import de.freese.spring.autoconfigure.TestAutoConfiguration;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@SpringBootTest(classes = TestH2Config.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
class TestH2ServerAutoConfiguration implements TestAutoConfiguration
{
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
