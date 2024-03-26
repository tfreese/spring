package de.freese.kubernetes.microservice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@RestController
public class MyRestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyRestController.class);

    private static String getHostName() {
        String hostName = null;

        try {
            hostName = InetAddress.getLocalHost().getHostName();
        }
        catch (Exception ex) {
            // Bei Betriebssystemen ohne DNS-Konfiguration funktioniert InetAddress.getLocalHost nicht !
            LOGGER.error(ex.getMessage(), ex);
        }

        if (hostName == null) {
            // Cross Platform (Windows, Linux, Unix, Mac)
            try (BufferedReader br = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(new String[]{"hostname"}).getInputStream(), StandardCharsets.UTF_8))) {
                hostName = br.readLine();
            }
            catch (Exception ex) {
                // Ignore
            }
        }

        if (hostName == null) {
            hostName = "unknown";
        }

        return hostName;
    }

    // private final DatabaseClient databaseClient;
    private final JdbcClient jdbcClient;

    public MyRestController(final JdbcClient jdbcClient) {
        super();

        // this.databaseClient = Objects.requireNonNull(databaseClient, "databaseClient required");
        this.jdbcClient = Objects.requireNonNull(jdbcClient, "jdbcClient required");
    }

    @GetMapping("/")
    public Publisher<String> greet() {
        final String message = "Hello World from %s: ".formatted(getHostName());

        return getDbTimestamp()
                .map(ts -> message + ts.toString())
                .onErrorReturn(message + LocalTime.now())
                ;
    }

    private Mono<LocalDateTime> getDbTimestamp() {
        // return databaseClient.sql("call LOCALTIMESTAMP").map((row, rowMetadata) -> row.get(0, LocalDateTime.class)).one();

        return Mono.just(jdbcClient.sql("call LOCALTIMESTAMP").query(rs -> {
            rs.next();
            final Timestamp timestamp = rs.getTimestamp(1);
            return timestamp.toLocalDateTime();
        }));
    }
}
