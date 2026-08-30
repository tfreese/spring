package de.freese.spring.kryo.reflection;

import java.time.LocalDateTime;

/**
 * @author Thomas Freese
 * @since 30.01.2020
 */
@FunctionalInterface
public interface ReflectionControllerApi {
    String INPUTSTREAM_IN_METHOD = "INPUTSTREAM_IN_METHOD";

    String OUTPUTSTREAM_IN_METHOD = "OUTPUTSTREAM_IN_METHOD";

    LocalDateTime testKryo();
}
