/**
 * Created: 28.01.2020
 */

package de.freese.spring.kryo.webflux;

import java.util.List;
import java.util.Map;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.HttpMessageDecoder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MimeType;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.util.Pool;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
public class KryoDecoder extends AbstractKryoCodecSupport implements HttpMessageDecoder<Object>
{
    /**
     * Erstellt ein neues {@link KryoDecoder} Object.
     *
     * @param kryoPool {@link Pool}<Kryo>
     */
    public KryoDecoder(final Pool<Kryo> kryoPool)
    {
        super(kryoPool);
    }

    /**
     * @see org.springframework.core.codec.Decoder#canDecode(org.springframework.core.ResolvableType, org.springframework.util.MimeType)
     */
    @Override
    public boolean canDecode(final ResolvableType elementType, final MimeType mimeType)
    {
        return Object.class.isAssignableFrom(elementType.toClass()) && supportsMimeType(mimeType);
        // return elementType.isInstance(Object.class) && supportsMimeType(mimeType);
    }

    /**
     * @see org.springframework.core.codec.Decoder#decode(org.springframework.core.io.buffer.DataBuffer, org.springframework.core.ResolvableType,
     *      org.springframework.util.MimeType, java.util.Map)
     */
    @Override
    public Object decode(final DataBuffer buffer, final ResolvableType targetType, final MimeType mimeType, final Map<String, Object> hints)
        throws DecodingException
    {
        Kryo kryo = getKryoPool().obtain();
        Object value = null;

        // try (Input input = new ByteBufferInput(buffer.asInputStream(),, 1024 * 1024))
        try (Input input = new Input(buffer.asInputStream(), 1024 * 1024))
        {
            value = kryo.readClassAndObject(input);
        }
        finally
        {
            getKryoPool().free(kryo);
        }

        return value;
    }

    /**
     * @see org.springframework.core.codec.Decoder#decode(org.reactivestreams.Publisher, org.springframework.core.ResolvableType,
     *      org.springframework.util.MimeType, java.util.Map)
     */
    @Override
    public Flux<Object> decode(final Publisher<DataBuffer> inputStream, final ResolvableType elementType, final MimeType mimeType,
                               final Map<String, Object> hints)
    {
        // return Flux.from(decodeToMono(inputStream, elementType, mimeType, hints));
        return Flux.from(inputStream).map(buffer -> decode(buffer, elementType, mimeType, hints));
    }

    /**
     * @see org.springframework.core.codec.Decoder#decodeToMono(org.reactivestreams.Publisher, org.springframework.core.ResolvableType,
     *      org.springframework.util.MimeType, java.util.Map)
     */
    @Override
    public Mono<Object> decodeToMono(final Publisher<DataBuffer> inputStream, final ResolvableType elementType, final MimeType mimeType,
                                     final Map<String, Object> hints)
    {
        // return DataBufferUtils.join(inputStream).map(dataBuffer -> decode(dataBuffer, elementType, mimeType, hints));
        return Mono.from(inputStream).map(buffer -> decode(buffer, elementType, mimeType, hints));
    }

    /**
     * @see org.springframework.core.codec.Decoder#getDecodableMimeTypes()
     */
    @Override
    public List<MimeType> getDecodableMimeTypes()
    {
        return MIME_TYPES;
    }

    /**
     * @see org.springframework.http.codec.HttpMessageDecoder#getDecodeHints(org.springframework.core.ResolvableType, org.springframework.core.ResolvableType,
     *      org.springframework.http.server.reactive.ServerHttpRequest, org.springframework.http.server.reactive.ServerHttpResponse)
     */
    @Override
    public Map<String, Object> getDecodeHints(final ResolvableType actualType, final ResolvableType elementType, final ServerHttpRequest request,
                                              final ServerHttpResponse response)
    {
        return Hints.none();
    }
}
