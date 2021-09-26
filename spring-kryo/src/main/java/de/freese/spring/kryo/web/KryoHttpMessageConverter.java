// Created: 22.05.2018
package de.freese.spring.kryo.web;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Supplier;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;

/**
 * @author Thomas Freese
 */
public class KryoHttpMessageConverter extends AbstractHttpMessageConverter<Object>
{
    /**
     * application/x-java-object; application/x-kryo
     */
    public static final MediaType APPLICATION_KRYO = MediaType.parseMediaType("application/x-kryo");
    /**
     * application/x-java-object; application/x-kryo
     */
    public static final String APPLICATION_KRYO_VALUE = "application/x-kryo";
    /**
     *
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    /**
     *
     */
    private final Pool<Kryo> kryoPool;

    /**
     * Erstellt ein neues {@link KryoHttpMessageConverter} Object.
     *
     * @param kryoPool {@link Supplier}
     */
    public KryoHttpMessageConverter(final Pool<Kryo> kryoPool)
    {
        super(DEFAULT_CHARSET, APPLICATION_KRYO); // DefaultContentType

        this.kryoPool = Objects.requireNonNull(kryoPool, "kryoPool required");
    }

    /**
     * @see org.springframework.http.converter.AbstractHttpMessageConverter#readInternal(java.lang.Class, org.springframework.http.HttpInputMessage)
     */
    @Override
    protected Object readInternal(final Class<? extends Object> clazz, final HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException
    {
        Kryo kryo = this.kryoPool.obtain();
        Object value = null;

        // try (Input input = new ByteBufferInput(inputMessage.getBody(), 1024 * 1024))
        try (Input input = new Input(inputMessage.getBody(), 1024 * 1024))
        {
            value = kryo.readClassAndObject(input);
        }
        finally
        {
            this.kryoPool.free(kryo);
        }

        return value;
    }

    /**
     * @see org.springframework.http.converter.AbstractHttpMessageConverter#supports(java.lang.Class)
     */
    @Override
    protected boolean supports(final Class<?> clazz)
    {
        return Object.class.isAssignableFrom(clazz);
    }

    /**
     * @see org.springframework.http.converter.AbstractHttpMessageConverter#writeInternal(java.lang.Object, org.springframework.http.HttpOutputMessage)
     */
    @Override
    protected void writeInternal(final Object t, final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException
    {
        Kryo kryo = this.kryoPool.obtain();

        // try (Output output = new ByteBufferOutput(outputMessage.getBody(), 1024 * 1024))
        try (Output output = new Output(outputMessage.getBody(), 1024 * 1024))
        {
            kryo.writeClassAndObject(output, t);
            output.flush();
        }
        finally
        {
            this.kryoPool.free(kryo);
        }
    }
}
