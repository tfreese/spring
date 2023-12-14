// Created: 28.01.2020
package de.freese.spring.kryo.webflux;

import java.util.List;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageEncoder;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

/**
 * @author Thomas Freese
 */
public class KryoEncoder extends AbstractKryoCodecSupport implements HttpMessageEncoder<Object> {
    public KryoEncoder(final Pool<Kryo> kryoPool) {
        super(kryoPool);
    }

    @Override
    public boolean canEncode(final ResolvableType elementType, final MimeType mimeType) {
        return Object.class.isAssignableFrom(elementType.toClass()) && supportsMimeType(mimeType);
        // return elementType.isInstance(Object.class) && supportsMimeType(mimeType);
    }

    @Override
    public Flux<DataBuffer> encode(final Publisher<? extends Object> inputStream, final DataBufferFactory bufferFactory, final ResolvableType elementType, final MimeType mimeType, final Map<String, Object> hints) {
        return Flux.from(inputStream).map(message -> encodeValue(message, bufferFactory, elementType, mimeType, hints));
    }

    @Override
    public DataBuffer encodeValue(final Object value, final DataBufferFactory bufferFactory, final ResolvableType valueType, final MimeType mimeType, final Map<String, Object> hints) {
        final DataBuffer buffer = bufferFactory.allocateBuffer(256);
        final Kryo kryo = getKryoPool().obtain();
        final boolean release = true;

        // try (Output output = new ByteBufferOutput(buffer.asOutputStream(), 1024 * 1024))
        try (Output output = new Output(buffer.asOutputStream(), 1024 * 1024)) {
            kryo.writeClassAndObject(output, value);
            output.flush();
        }
        finally {
            getKryoPool().free(kryo);

            if (release) {
                DataBufferUtils.release(buffer);
            }
        }

        return buffer;
    }

    @Override
    public List<MimeType> getEncodableMimeTypes() {
        return MIME_TYPES;
    }

    @Override
    public List<MediaType> getStreamingMediaTypes() {
        return MEDIA_TYPES;
    }
}
