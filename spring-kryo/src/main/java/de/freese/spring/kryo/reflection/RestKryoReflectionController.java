// Created: 22.05.2018
package de.freese.spring.kryo.reflection;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.freese.spring.kryo.KryoPool;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("reflection/ReflectionControllerApi")
public class RestKryoReflectionController extends AbstractRestReflectionController implements ReflectionControllerApi {
    public RestKryoReflectionController(final KryoPool kryoPool) {
        super(kryoPool);
    }

    @Override
    public LocalDateTime testKryo() {
        return LocalDateTime.now();
    }
}
