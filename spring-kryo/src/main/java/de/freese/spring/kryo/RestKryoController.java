// Created: 22.05.2018
package de.freese.spring.kryo;

import java.time.LocalDateTime;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.freese.spring.kryo.web.KryoHttpMessageConverter;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("/")
public class RestKryoController {
    /**
     * curl -X GET "http://localhost:8081/json"
     */
    @GetMapping(path = "json", produces = MediaType.APPLICATION_JSON_VALUE)
    public LocalDateTime testJson() {
        return LocalDateTime.now();
    }

    /**
     * curl -X GET "http://localhost:8081/kryo" --output -
     */
    @GetMapping(path = "kryo", produces = KryoHttpMessageConverter.APPLICATION_KRYO_VALUE)
    public LocalDateTime testKryo() {
        return LocalDateTime.now();
    }
}
