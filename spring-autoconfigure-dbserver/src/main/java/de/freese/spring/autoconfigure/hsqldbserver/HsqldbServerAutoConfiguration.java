// Created: 13.02.2017
package de.freese.spring.autoconfigure.hsqldbserver;

import java.util.List;

import jakarta.annotation.Resource;

import org.hsqldb.Database;
import org.hsqldb.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.freese.spring.autoconfigure.hsqldbserver.HsqldbServerProperties.DB;

/**
 * AutoConfiguration for a HSQLDB-{@link Server}.<br>
 * Server {@link Server} is creratedm when not already running.<br>
 * Executed before {@link DataSourceAutoConfiguration}.<br>
 * <br>
 * Example:
 *
 * <pre>
 * With Properties:
 * hsqldb.server.enabled = true
 * hsqldb.server.port = ${port}
 * hsqldb.server.noSystemExit = true
 * hsqldb.server.silent = true
 * hsqldb.server.trace = false
 * hsqldb.server.db[0].name=${name0}
 * hsqldb.server.db[0].path=file:/${path}/${name0}
 * hsqldb.server.db[1].name=${name1}
 * hsqldb.server.db[1].path=mem:${name1}
 *
 * With YAML:
 * hsqldb:
 *     server:
 *         enabled: true
 *         port: ${port}
 *         noSystemExit: true
 *         silent: true
 *         trace: false
 *         db:
 *             - name: ${name0}
 *               path: file:/${path}/${name0}
 *             - name: ${name1}
 *               path: mem:${name1}
 * </pre>
 *
 * @author Thomas Freese
 */
@Configuration
@ConditionalOnClass(Server.class) // Only when HSQLDB is in Classpath.
@ConditionalOnMissingBean(Server.class) // Only when Server is not in the SpringContext.
@ConditionalOnProperty(prefix = "hsqldb.server", name = "enabled", matchIfMissing = false) // Only if enabled.
@EnableConfigurationProperties(HsqldbServerProperties.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
public class HsqldbServerAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(HsqldbServerAutoConfiguration.class);

    @Resource
    private HsqldbServerProperties hsqldbServerProperties;

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    // @Scope(ConfigurableBeanFactory#SCOPE_SINGLETON)
    public Server hsqldbServer() throws Exception {
        final int port = this.hsqldbServerProperties.getPort();
        final boolean noSystemExit = this.hsqldbServerProperties.isNoSystemExit();
        final boolean silent = this.hsqldbServerProperties.isSilent();
        final boolean trace = this.hsqldbServerProperties.isTrace();
        final List<DB> dbs = this.hsqldbServerProperties.getDb();

        if (LOGGER.isInfoEnabled()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Create HsqldbServer with:");
            sb.append(" port={}");
            sb.append(", noSystemExit={}");
            sb.append(", silent={}");
            sb.append(", trace={}");
            sb.append(", dataBases={}");

            LOGGER.info(sb.toString(), port, noSystemExit, silent, trace, dbs);
        }

        final Server server = new Server() {
            @Override
            public void shutdown() {
                // "SHUTDOWN COMPACT"
                super.shutdownWithCatalogs(Database.CLOSEMODE_COMPACT);
            }
        };
        server.setLogWriter(null);
        server.setErrWriter(null);
        // server.setLogWriter(new PrintWriter(System.out)); // can use custom writer
        // server.setErrWriter(new PrintWriter(System.err)); // can use custom writer
        server.setNoSystemExit(noSystemExit);
        server.setSilent(silent);
        server.setTrace(trace);

        // server.setAddress("0.0.0.0");
        server.setPort(port);

        for (int i = 0; i < dbs.size(); i++) {
            final DB db = dbs.get(i);

            server.setDatabaseName(i, db.getName());
            server.setDatabasePath(i, db.getPath());
        }

        return server;
    }
}
