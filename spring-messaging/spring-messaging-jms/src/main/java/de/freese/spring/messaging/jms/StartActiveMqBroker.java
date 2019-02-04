/**
 * Created: 04.02.2019
 */

package de.freese.spring.messaging.jms;

import java.io.File;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.store.PList;
import org.apache.activemq.store.PListStore;
import org.apache.activemq.store.PersistenceAdapter;
import org.apache.activemq.store.memory.MemoryPersistenceAdapter;

/**
 * Stand-Alone Broker Instanz von ActiveMQ.
 * 
 * @author Thomas Freese
 */
public class StartActiveMqBroker
{
    /**
     * @param args String[]
     * @throws Exception Falls was schief geht.
     */
    public static void main(final String[] args) throws Exception
    {
        BrokerService broker = new BrokerService();
        PersistenceAdapter persistenceAdapter = new MemoryPersistenceAdapter();
        // persistenceAdapter.setDirectory(new File("activemq"));

        broker.setPersistenceAdapter(persistenceAdapter);
        broker.setUseJmx(false);
        broker.addConnector("tcp://localhost:" + BrokerService.DEFAULT_PORT);
        broker.setUseShutdownHook(true);
        broker.setTempDataStore(new PListStore()
        {
            @Override
            public File getDirectory()
            {
                return null;
            }

            @Override
            public PList getPList(final String name) throws Exception
            {
                return null;
            }

            @Override
            public boolean removePList(final String name) throws Exception
            {
                return false;
            }

            @Override
            public void setDirectory(final File directory)
            {
                // // NO-OP
            }

            @Override
            public long size()
            {
                return 0;
            }

            @Override
            public void start() throws Exception
            {
                // NO-OP
            }

            @Override
            public void stop() throws Exception
            {
                // NO-OP
            }
        });
        broker.start();
        broker.waitUntilStopped();
    }

    /**
     * Erstellt ein neues {@link StartActiveMqBroker} Object.
     */
    private StartActiveMqBroker()
    {
        super();
    }
}
