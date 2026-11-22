package de.freese.spring.kryo.webflux;

import java.util.List;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBuffers;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageEncoder;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import de.freese.spring.kryo.KryoPool;

/**
 * @author Thomas Freese
 * @since 28.01.2020
 */
public class KryoEncoder extends AbstractKryoCodecSupport implements HttpMessageEncoder<Object> {
    private static final boolean RELEASE_BUFFER = true;

    public KryoEncoder(final KryoPool kryoPool) {
        super(kryoPool);
    }

    @Override
    public boolean canEncode(final ResolvableType elementType, final MimeType mimeType) {
        return Object.class.isAssignableFrom(elementType.toClass()) && supportsMimeType(mimeType);
        // return elementType.isInstance(Object.class) && supportsMimeType(mimeType);
    }

    @Override
    public @NullMarked Flux<DataBuffer> encode(final Publisher<? extends Object> inputStream,
                                               final DataBufferFactory bufferFactory,
                                               final ResolvableType elementType,
                                               final @Nullable MimeType mimeType,
                                               final @Nullable Map<String, Object> hints) {
        return Flux.from(inputStream).map(message -> encodeValue(message, bufferFactory, elementType, mimeType, hints));
    }

    @Override
    public @NullMarked DataBuffer encodeValue(final Object value,
                                              final DataBufferFactory bufferFactory,
                                              final ResolvableType valueType,
                                              final @Nullable MimeType mimeType,
                                              final @Nullable Map<String, Object> hints) {
        final DataBuffer buffer = bufferFactory.allocateBuffer(256);
        final Kryo kryo = getKryoPool().getKryo();

        // try (Output output = new ByteBufferOutput(buffer.asOutputStream(), DEFAULT_BUFFER_SIZE))
        try (Output output = new Output(buffer.asOutputStream(), DEFAULT_BUFFER_SIZE)) {
            kryo.writeClassAndObject(output, value);
            output.flush();
        }
        finally {
            getKryoPool().returnKryo(kryo);

            if (RELEASE_BUFFER) {
                DataBuffers.release(buffer);
            }
        }

        return buffer;
    }

    @Override
    public @NullMarked List<MimeType> getEncodableMimeTypes() {
        return MIME_TYPES;
    }

    @Override
    public @NullMarked List<MediaType> getStreamingMediaTypes() {
        return MEDIA_TYPES;
    }
}
