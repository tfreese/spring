// Created: 28.01.2020
package de.freese.spring.kryo.webflux;

import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageEncoder;
import org.springframework.util.MimeType;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;

import reactor.core.publisher.Flux;

/**
 * @author Thomas Freese
 */
public class KryoEncoder extends AbstractKryoCodecSupport implements HttpMessageEncoder<Object>
{
    /**
     * Erstellt ein neues {@link KryoEncoder} Object.
     *
     * @param kryoPool {@link Pool}<Kryo>
     */
    public KryoEncoder(final Pool<Kryo> kryoPool)
    {
        super(kryoPool);
    }

    /**
     * @see org.springframework.core.codec.Encoder#canEncode(org.springframework.core.ResolvableType, org.springframework.util.MimeType)
     */
    @Override
    public boolean canEncode(final ResolvableType elementType, final MimeType mimeType)
    {
        return Object.class.isAssignableFrom(elementType.toClass()) && supportsMimeType(mimeType);
        // return elementType.isInstance(Object.class) && supportsMimeType(mimeType);
    }

    /**
     * @see org.springframework.core.codec.Encoder#encode(org.reactivestreams.Publisher, org.springframework.core.io.buffer.DataBufferFactory,
     *      org.springframework.core.ResolvableType, org.springframework.util.MimeType, java.util.Map)
     */
    @Override
    public Flux<DataBuffer> encode(final Publisher<? extends Object> inputStream, final DataBufferFactory bufferFactory, final ResolvableType elementType,
                                   final MimeType mimeType, final Map<String, Object> hints)
    {
        return Flux.from(inputStream).map(message -> encodeValue(message, bufferFactory, elementType, mimeType, hints));
    }

    /**
     * @see org.springframework.core.codec.Encoder#encodeValue(java.lang.Object, org.springframework.core.io.buffer.DataBufferFactory,
     *      org.springframework.core.ResolvableType, org.springframework.util.MimeType, java.util.Map)
     */
    @Override
    public DataBuffer encodeValue(final Object value, final DataBufferFactory bufferFactory, final ResolvableType valueType, final MimeType mimeType,
                                  final Map<String, Object> hints)
    {
        DataBuffer buffer = bufferFactory.allocateBuffer();
        Kryo kryo = getKryoPool().obtain();
        boolean release = true;

        // try (Output output = new ByteBufferOutput(buffer.asOutputStream(), 1024 * 1024))
        try (Output output = new Output(buffer.asOutputStream(), 1024 * 1024))
        {
            kryo.writeClassAndObject(output, value);
            output.flush();
        }
        finally
        {
            getKryoPool().free(kryo);

            if (release)
            {
                DataBufferUtils.release(buffer);
            }
        }

        return buffer;
    }

    /**
     * @see org.springframework.core.codec.Encoder#getEncodableMimeTypes()
     */
    @Override
    public List<MimeType> getEncodableMimeTypes()
    {
        return MIME_TYPES;
    }

    /**
     * @see org.springframework.http.codec.HttpMessageEncoder#getStreamingMediaTypes()
     */
    @Override
    public List<MediaType> getStreamingMediaTypes()
    {
        return MEDIA_TYPES;
    }
}
