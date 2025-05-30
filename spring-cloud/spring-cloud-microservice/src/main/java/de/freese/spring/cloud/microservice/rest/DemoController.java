// Created: 14.02.2017
package de.freese.spring.cloud.microservice.rest;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * curl -X GET -H "Accept: application/json" -v "http://localhost:8081"<br>
 * curl -X GET "http://localhost:8081"<br>
 * curl http://localhost:8081/actuator/health
 *
 * @author Thomas Freese
 */
@RestController
@RequestMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class DemoController {
    @Value("${server.port}")
    private int port = -1;

    @GetMapping("/")
    public String hello() throws UnknownHostException {
        return String.format("{ \"message\": \"Hello from %s:%d\" }", InetAddress.getLocalHost(), port);
    }

    @GetMapping("/ping")
    public String ping() throws UnknownHostException {
        return String.format("{ \"message\": \"Ping from %s:%d\" }", InetAddress.getLocalHost(), port);
    }
}
