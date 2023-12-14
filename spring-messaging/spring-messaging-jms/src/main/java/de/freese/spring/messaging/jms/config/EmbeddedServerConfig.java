// Created: 22.10.22
package de.freese.spring.messaging.jms.config;

import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Thomas Freese
 */
@Configuration
//@Profile("!test")
public class EmbeddedServerConfig {
    @Bean(initMethod = "start", destroyMethod = "stop")
    public EmbeddedActiveMQ embeddedActiveMQ() throws Exception {
        final org.apache.activemq.artemis.core.config.Configuration config = new ConfigurationImpl();

        config.addAcceptorConfiguration("in-vm", "vm://0");
        //        config.addAcceptorConfiguration("tcp", "tcp://127.0.0.1:61616");
        config.setSecurityEnabled(false);

        // Kein Zwischen-Speichern von Daten.
        config.setPersistenceEnabled(false);

        //        final Path storePath = Paths.get(".activeMQ").toAbsolutePath();
        //        config.setBindingsDirectory(storePath.resolve("bindings").toString());
        //        config.setJournalDirectory(storePath.resolve("journal").toString());
        //        config.setLargeMessagesDirectory(storePath.resolve("largeMessages").toString());
        //        config.setPagingDirectory(storePath.resolve("paging").toString());

        final EmbeddedActiveMQ server = new EmbeddedActiveMQ();
        server.setConfiguration(config);
        //        ActiveMQServer server = new ActiveMQServerImpl(config);

        return server;
    }
}
