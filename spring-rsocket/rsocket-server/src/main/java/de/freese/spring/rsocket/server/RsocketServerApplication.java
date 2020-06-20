/**
 * Created: 11.03.2020
 */

package de.freese.spring.rsocket.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public class RsocketServerApplication
{
    /**
     * @param args String[]
     */
    @SuppressWarnings("resource")
    public static void main(final String[] args)
    {
        SpringApplication.run(RsocketServerApplication.class, args);
    }
}
