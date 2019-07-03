// Created: 14.02.2017
package de.freese.spring.microservice;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * curl -X GET -H "Accept: application/json" -v "http://localhost:PORT/service/clock"<br>
 * curl -X GET "http://localhost:PORT/service/clock"
 *
 * @author Thomas Freese
 */
// @RestController
// @RequestMapping(path = "/service", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@RestControllerForJSON("/service")
public class RestService
{
    /**
     * http://lewandowski.io/2016/02/formatting-java-time-with-spring-boot-using-json/
     *
     * @author Thomas Freese
     */
    public static class Clock
    {
        /**
        *
        */
        private final Date date;

        /**
         *
         */
        private final LocalDate localDate;

        /**
         *
         */
        // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private final LocalDateTime localDateTime;

        /**
         *
         */
        private final LocalTime localTime;

        /**
         * Erstellt ein neues {@link Clock} Object.
         */
        public Clock()
        {
            super();

            this.date = new Date();
            this.localDate = LocalDate.now();
            this.localTime = LocalTime.now();
            this.localDateTime = LocalDateTime.now();
        }

        /**
         * @return {@link Date}
         */
        public Date getDate()
        {
            return this.date;
        }

        /**
         * @return {@link LocalDate}
         */
        public LocalDate getLocalDate()
        {
            return this.localDate;
        }

        /**
         * @return {@link LocalDateTime}
         */
        public LocalDateTime getLocalDateTime()
        {
            return this.localDateTime;
        }

        /**
         * @return {@link LocalTime}
         */
        public LocalTime getLocalTime()
        {
            return this.localTime;
        }
    }

    /**
     *
     */
    // @LocalServerPort
    @Value("${server.port}")
    private int port = -1;

    /**
     * Erzeugt eine neue Instanz von {@link RestService}
     */
    public RestService()
    {
        super();
    }

    /**
     * http://localhost:PORT/service/clock/
     *
     * @return {@link Clock}
     */
    @GetMapping(path = "/clock", produces =
    {
            MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.APPLICATION_XML_VALUE
    })
    public Clock clock()
    {
        Clock clock = new Clock();

        return clock;
    }

    /**
     * http://localhost:PORT/service/ping/
     *
     * @return boolean
     */
    @GetMapping("/ping")
    public boolean ping()
    {
        return true;
    }

    /**
     * http://localhost:PORT/service/sysdate/
     *
     * @return String
     * @throws UnknownHostException Falls was schief geht.
     */
    @GetMapping("/sysdate")
    public String sysdate() throws UnknownHostException
    {
        String sysDate = LocalDateTime.now().toString() + " on " + InetAddress.getLocalHost() + "@" + this.port;

        return sysDate + "\n";
    }
}
