/**
 * Created: 30.01.2020
 */

package de.freese.spring.kryo.reflection;

import java.time.LocalDateTime;

/**
 * @author Thomas Freese
 */
@FunctionalInterface
public interface ReflectionControllerApi
{
    /**
     *
     */
    public static final String INPUTSTREAM_IN_METHOD = "INPUTSTREAM_IN_METHOD";

    /**
     *
     */
    public static final String OUTPUTSTREAM_IN_METHOD = "OUTPUTSTREAM_IN_METHOD";

    /**
     * @return {@link LocalDateTime}
     */
    public LocalDateTime testKryo();
}
