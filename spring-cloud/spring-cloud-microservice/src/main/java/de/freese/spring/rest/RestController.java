// Created: 14.02.2017
package de.freese.spring.rest;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * curl -X GET -H "Accept: application/json" -v "http://localhost:8081"<br>
 * curl -X GET "http://localhost:8081"<br>
 * curl http://localhost:8081/actuator/metrics/http.server.requests
 *
 * @author Thomas Freese
 */
@org.springframework.web.bind.annotation.RestController
@RequestMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class RestController
{
    /**
     *
     */
    @Value("${server.port}")
    private int port = -1;

    /**
     * http://localhost:PORT/ping/
     *
     * @return boolean
     *
     * @throws UnknownHostException Falls was schiefgeht.
     */
    @GetMapping("/ping")
    public String ping() throws UnknownHostException
    {
        return String.format("{ \"message\": \"Ping from %s:%d\" }", InetAddress.getLocalHost(), this.port);
    }

    /**
     * http://localhost:PORT
     *
     * @return String
     *
     * @throws UnknownHostException Falls was schiefgeht.
     */
    @GetMapping("/")
    public String hello() throws UnknownHostException
    {
        return String.format("{ \"message\": \"Hello from %s:%d\" }", InetAddress.getLocalHost(), this.port);
    }
}
