// Created: 29.01.2020
package de.freese.spring.kryo.webflux;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.Pool;
import de.freese.spring.kryo.web.KryoHttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;

/**
 * @author Thomas Freese
 */
public abstract class AbstractKryoCodecSupport
{
    /**
     * application/x-java-object; application/x-kryo
     */
    public static final MimeType APPLICATION_KRYO = MimeType.valueOf(KryoHttpMessageConverter.APPLICATION_KRYO_VALUE);

    protected static final List<MediaType> MEDIA_TYPES =
            Stream.of(APPLICATION_KRYO).map(mimeType -> new MediaType(mimeType.getType(), mimeType.getSubtype())).toList();

    protected static final List<MimeType> MIME_TYPES = List.of(APPLICATION_KRYO);

    private final Pool<Kryo> kryoPool;

    protected AbstractKryoCodecSupport(final Pool<Kryo> kryoPool)
    {
        super();

        this.kryoPool = Objects.requireNonNull(kryoPool, "kryoPool required");
    }

    protected Pool<Kryo> getKryoPool()
    {
        return this.kryoPool;
    }

    protected boolean supportsMimeType(final MimeType mimeType)
    {
        return ((mimeType == null) || MIME_TYPES.stream().anyMatch(m -> m.isCompatibleWith(mimeType)));
    }
}
