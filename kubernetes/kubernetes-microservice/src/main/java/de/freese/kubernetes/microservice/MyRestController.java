package de.freese.kubernetes.microservice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@RestController
public class MyRestController
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MyRestController.class);

    /**
     * @param name {@link Optional}
     *
     * @return {@link Publisher}
     */
    @GetMapping("greet")
    public Publisher<String> greet(@RequestParam final Optional<String> name)
    {
        String hostName = getHostName();

        //@formatter:off
        return name
                .map(s -> {
                    var msg = "Hello " + s + " on " + hostName;
                    return Mono.just(msg);
                    })
                //.orElse(Mono.error(new NullPointerException("name")))
                .orElse(Mono.just("Hello World on " +  hostName))
                ;
        //@formatter:on
    }

    /**
     * @return String
     */
    private static String getHostName()
    {
        String hostName = null;

        try
        {
            hostName = InetAddress.getLocalHost().getHostName();
        }
        catch (Exception ex)
        {
            // Bei Betriebssystemen ohne DNS-Konfiguration funktioniert InetAddress.getLocalHost nicht !
            LOGGER.error(ex.getMessage(), ex);
        }

        if (hostName == null)
        {
            // Cross Platform (Windows, Linux, Unix, Mac)
            try (BufferedReader br = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(new String[]{"hostname"}).getInputStream(), StandardCharsets.UTF_8)))
            {
                hostName = br.readLine();
            }
            catch (Exception ex)
            {
                // Ignore
            }
        }

        if (hostName == null)
        {
            hostName = "unknown";
        }

        return hostName;
    }
}
