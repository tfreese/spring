package de.freese.spring.messaging.jms.config;

import org.apache.activemq.artemis.core.server.JournalType;
import org.springframework.boot.artemis.autoconfigure.ArtemisConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author Thomas Freese
 * @since 22.10.2022
 */
@Configuration
@Profile("!test")
public class EmbeddedServerConfig {
    @Bean
    public ArtemisConfigurationCustomizer artemisCustomizer() {
        return configuration -> {
            configuration.setPersistenceEnabled(false);

            configuration.setJournalDirectory("./build/artemis-data/journal");
            configuration.setBindingsDirectory("./build/artemis-data/bindings");
            configuration.setPagingDirectory("./build/artemis-data/paging");
            configuration.setLargeMessagesDirectory("./build/artemis-data/largemessages");

            configuration.setJournalType(JournalType.NIO);
            configuration.setDiskScanPeriod(5000);
            configuration.setMaxDiskUsage(95);
        };
    }
    //
    // @Bean(initMethod = "start", destroyMethod = "stop")
    // public EmbeddedActiveMQ embeddedActiveMQ() throws Exception {
    //     final org.apache.activemq.artemis.core.config.Configuration config = new ConfigurationImpl();
    //
    //     config.addAcceptorConfiguration("in-vm", "vm://0");
    //     // config.addAcceptorConfiguration("tcp", "tcp://127.0.0.1:61616");
    //     config.setSecurityEnabled(false);
    //
    //     // Kein Zwischen-Speichern von Daten.
    //     config.setPersistenceEnabled(false);
    //
    //     //        final Path storePath = Paths.get(".activeMQ").toAbsolutePath();
    //     //        config.setBindingsDirectory(storePath.resolve("bindings").toString());
    //     //        config.setJournalDirectory(storePath.resolve("journal").toString());
    //     //        config.setLargeMessagesDirectory(storePath.resolve("largeMessages").toString());
    //     //        config.setPagingDirectory(storePath.resolve("paging").toString());
    //
    //     final EmbeddedActiveMQ server = new EmbeddedActiveMQ();
    //     server.setConfiguration(config);
    //
    //     return server;
    // }
}
