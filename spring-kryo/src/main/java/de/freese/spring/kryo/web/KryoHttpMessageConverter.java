// Created: 22.05.2018
package de.freese.spring.kryo.web;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * @author Thomas Freese
 */
public class KryoHttpMessageConverter extends AbstractHttpMessageConverter<Object> {
    /**
     * application/x-java-object; application/x-kryo
     */
    public static final MediaType APPLICATION_KRYO = MediaType.parseMediaType("application/x-kryo");
    /**
     * application/x-java-object; application/x-kryo
     */
    public static final String APPLICATION_KRYO_VALUE = "application/x-kryo";
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * 1 MB
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

    private final Pool<Kryo> kryoPool;

    public KryoHttpMessageConverter(final Pool<Kryo> kryoPool) {
        super(DEFAULT_CHARSET, APPLICATION_KRYO); // DefaultContentType

        this.kryoPool = Objects.requireNonNull(kryoPool, "kryoPool required");
    }

    @Override
    protected Object readInternal(final Class<? extends Object> clazz, final HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        final Kryo kryo = kryoPool.obtain();
        Object value = null;

        // try (Input input = new ByteBufferInput(inputMessage.getBody(), DEFAULT_BUFFER_SIZE))
        try (Input input = new Input(inputMessage.getBody(), DEFAULT_BUFFER_SIZE)) {
            value = kryo.readClassAndObject(input);
        }
        finally {
            kryoPool.free(kryo);
        }

        return value;
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return Object.class.isAssignableFrom(clazz);
    }

    @Override
    protected void writeInternal(final Object t, final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        final Kryo kryo = kryoPool.obtain();

        // try (Output output = new ByteBufferOutput(outputMessage.getBody(), DEFAULT_BUFFER_SIZE))
        try (Output output = new Output(outputMessage.getBody(), DEFAULT_BUFFER_SIZE)) {
            kryo.writeClassAndObject(output, t);
            output.flush();
        }
        finally {
            kryoPool.free(kryo);
        }
    }
}
