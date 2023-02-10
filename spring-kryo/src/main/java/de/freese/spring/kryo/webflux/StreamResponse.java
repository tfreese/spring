// Created: 07.02.2020
package de.freese.spring.kryo.webflux;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * @author Thomas Freese
 */
public final class StreamResponse {
    public static StreamingResponseBody ok(final byte[] data) {
        return outputStream -> outputStream.write(data);
    }

    public static StreamingResponseBody ok(final Consumer<Writer> consumer) {
        return outputStream -> {
            try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                consumer.accept(writer);
            }
        };
    }

    public static StreamingResponseBody ok(final InputStream inputStream) {
        return outputStream -> {
            byte[] buffer = new byte[4096];

            for (int n = 0; n >= 0; n = inputStream.read(buffer)) {
                outputStream.write(buffer, 0, n);
            }
        };
    }

    public static StreamingResponseBody ok(final Object object, final Kryo kryo) {
        return outputStream -> {
            try (Output output = new Output(outputStream)) {
                kryo.writeClassAndObject(output, object);
            }
        };
    }

    private StreamResponse() {
        super();
    }
}
