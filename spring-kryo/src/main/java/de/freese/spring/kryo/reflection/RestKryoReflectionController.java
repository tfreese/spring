// Created: 22.05.2018
package de.freese.spring.kryo.reflection;

import java.time.LocalDateTime;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.Pool;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("reflection/ReflectionControllerApi")
public class RestKryoReflectionController extends AbstractRestReflectionController implements ReflectionControllerApi {
    public RestKryoReflectionController(final Pool<Kryo> kryoPool) {
        super(kryoPool);
    }

    @Override
    public LocalDateTime testKryo() {
        return LocalDateTime.now();
    }
}
