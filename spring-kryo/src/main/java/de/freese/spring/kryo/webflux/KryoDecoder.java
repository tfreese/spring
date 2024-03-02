// Created: 28.01.2020
package de.freese.spring.kryo.webflux;

import java.util.List;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.util.Pool;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.HttpMessageDecoder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
public class KryoDecoder extends AbstractKryoCodecSupport implements HttpMessageDecoder<Object> {
    public KryoDecoder(final Pool<Kryo> kryoPool) {
        super(kryoPool);
    }

    @Override
    public boolean canDecode(final ResolvableType elementType, final MimeType mimeType) {
        return Object.class.isAssignableFrom(elementType.toClass()) && supportsMimeType(mimeType);
        // return elementType.isInstance(Object.class) && supportsMimeType(mimeType);
    }

    @Override
    public Object decode(final DataBuffer buffer, final ResolvableType targetType, final MimeType mimeType, final Map<String, Object> hints) throws DecodingException {
        final Kryo kryo = getKryoPool().obtain();
        Object value = null;

        // try (Input input = new ByteBufferInput(buffer.asInputStream(), DEFAULT_BUFFER_SIZE))
        try (Input input = new Input(buffer.asInputStream(), DEFAULT_BUFFER_SIZE)) {
            value = kryo.readClassAndObject(input);
        }
        finally {
            getKryoPool().free(kryo);
        }

        return value;
    }

    @Override
    public Flux<Object> decode(final Publisher<DataBuffer> inputStream, final ResolvableType elementType, final MimeType mimeType, final Map<String, Object> hints) {
        // return Flux.from(decodeToMono(inputStream, elementType, mimeType, hints));
        return Flux.from(inputStream).map(buffer -> decode(buffer, elementType, mimeType, hints));
    }

    @Override
    public Mono<Object> decodeToMono(final Publisher<DataBuffer> inputStream, final ResolvableType elementType, final MimeType mimeType, final Map<String, Object> hints) {
        // return DataBufferUtils.join(inputStream).map(dataBuffer -> decode(dataBuffer, elementType, mimeType, hints));
        return Mono.from(inputStream).map(buffer -> decode(buffer, elementType, mimeType, hints));
    }

    @Override
    public List<MimeType> getDecodableMimeTypes() {
        return MIME_TYPES;
    }

    @Override
    public Map<String, Object> getDecodeHints(final ResolvableType actualType, final ResolvableType elementType, final ServerHttpRequest request,
                                              final ServerHttpResponse response) {
        return Hints.none();
    }
}
