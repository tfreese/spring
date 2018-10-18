/**
 * Created: 22.05.2018
 */

package de.freese.spring.kryo;

import java.time.LocalDateTime;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("/kryo")
public class RestService
{
    /**
     * Erstellt ein neues {@link RestService} Object.
     */
    public RestService()
    {
        super();
    }

    /**
     * http://8081/kryo/test2<br>
     * curl -X GET "http://localhost:8081/kryo/test2" --output -
     *
     * @return {@link LocalDateTime}
     */
    @GetMapping(path = "/test2", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public LocalDateTime testJson()
    {
        LocalDateTime localDateTime = LocalDateTime.now();

        return localDateTime;
    }

    /**
     * http://8081/kryo/test<br>
     * curl -X GET "http://localhost:8081/kryo/test"
     *
     * @return {@link LocalDateTime}
     */
    @GetMapping(path = "/test", produces = KryoHttpMessageConverter.APPLICATION_KRYO_VALUE)
    public LocalDateTime testKryo()
    {
        LocalDateTime localDateTime = LocalDateTime.now();

        return localDateTime;
    }
}