/**
 * Created: 07.02.2020
 */

package de.freese.spring.kryo.webflux;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author Thomas Freese
 */
public final class StreamResponse
{
    /**
     * @param data byte[]
     * @return {@link StreamingResponseBody}
     */
    public static StreamingResponseBody ok(final byte[] data)
    {
        return outputStream -> outputStream.write(data);
    }

    /**
     * @param consumer {@link Consumer}
     * @return {@link StreamingResponseBody}
     */
    public static StreamingResponseBody ok(final Consumer<Writer> consumer)
    {
        return outputStream -> {
            try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))
            {
                consumer.accept(writer);
            }
        };
    }

    /**
     * @param inputStream {@link InputStream}
     * @return {@link StreamingResponseBody}
     */
    public static StreamingResponseBody ok(final InputStream inputStream)
    {
        return outputStream -> {
            byte[] buffer = new byte[4096];

            for (int n = 0; n >= 0; n = inputStream.read(buffer))
            {
                outputStream.write(buffer, 0, n);
            }
        };
    }

    /**
     * @param object Object
     * @param kryo {@link Kryo}
     * @return {@link StreamingResponseBody}
     */
    public static StreamingResponseBody ok(final Object object, final Kryo kryo)
    {
        return outputStream -> {
            try (Output output = new Output(outputStream))
            {
                kryo.writeClassAndObject(output, object);
            }
        };
    }

    /**
     * Erstellt ein neues {@link StreamResponse} Object.
     */
    private StreamResponse()
    {
        super();
    }
}
