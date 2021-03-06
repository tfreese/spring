/**
 * Created: 03.02.2019
 */

package org.spring.messaging.amqp.qpid;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.qpid.server.SystemLauncher;
import org.apache.qpid.server.model.ConfiguredObject;
import org.apache.qpid.server.model.SystemConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Thomas Freese
 */
@Configuration
public class QpidConfig
{
    /**
     *
     */
    private static final String AMQP_PORT = "5672";

    /**
     *
     */
    private static final String INITIAL_CONFIGURATION = "qpid-config.json";

    /**
     *
     */
    private static final String QPID_HOME_DIR = System.getProperty("user.dir");

    /**
    *
    */
    private static final String QPID_WORK_DIR = QPID_HOME_DIR + File.separator + "qpid-work";

    // /**
    // * @param connectionFactory {@link ConnectionFactory}
    // * @param listenerAdapter {@link MessageListenerAdapter}
    // * @return {@link MessageListenerContainer}, final MessageListenerAdapter listenerAdapter
    // */
    // @Bean
    // public MessageListenerContainer container(final ConnectionFactory connectionFactory)
    // {
    // SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    // container.setConnectionFactory(connectionFactory);
    // container.setQueueNames(SpringQpidApplication.queueName);
    // // container.setMessageListener(listenerAdapter);
    //
    // return container;
    // }

    // /**
    // * @param receiver {@link Receiver}
    // * @return {@link MessageListenerAdapter}
    // */
    // @Bean
    // public MessageListenerAdapter listenerAdapter(final Receiver receiver)
    // {
    // return new MessageListenerAdapter(receiver, "receiveMessage");
    // }

    /**
     * @return {@link SystemLauncher}
     * @throws Exception Falls was schief geht.
     */
    @Bean
    public SystemLauncher systemLauncher() throws Exception
    {
        URL initialConfig = ClassLoader.getSystemClassLoader().getResource(INITIAL_CONFIGURATION);
        System.setProperty("QPID_HOME", QPID_HOME_DIR);
        // System.setProperty("QPID_WORK", QPID_WORK_DIR);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(ConfiguredObject.TYPE, "Memory");
        attributes.put(SystemConfig.INITIAL_CONFIGURATION_LOCATION, initialConfig.toExternalForm());
        attributes.put(SystemConfig.STARTUP_LOGGED_TO_SYSTEM_OUT, true);
        attributes.put(SystemConfig.QPID_WORK_DIR, QPID_WORK_DIR);
        attributes.put("qpid.home_dir", QPID_HOME_DIR);
        attributes.put("qpid.amqp_port", AMQP_PORT);
        attributes.put("qpid.http_port", "8080");

        SystemLauncher systemLauncher = new SystemLauncher();
        systemLauncher.startup(attributes);

        return systemLauncher;
    }
}
