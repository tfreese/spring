/**
 * Created: 29.01.2020
 */

package de.freese.spring.kryo.webflux;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.Pool;
import de.freese.spring.kryo.web.KryoHttpMessageConverter;

/**
 * @author Thomas Freese
 */
public abstract class KryoCodecSupport
{
    /**
     * application/x-java-object; application/x-kryo
     */
    public static final MimeType APPLICATION_KRYO = MimeType.valueOf(KryoHttpMessageConverter.APPLICATION_KRYO_VALUE);

    /**
     *
     */
    protected static final List<MediaType> MEDIA_TYPES =
            Arrays.asList(APPLICATION_KRYO).stream().map(mimeType -> new MediaType(mimeType.getType(), mimeType.getSubtype())).collect(Collectors.toList());

    /**
     *
     */
    protected static final List<MimeType> MIME_TYPES = Collections.unmodifiableList(Arrays.asList(APPLICATION_KRYO));

    /**
    *
    */
    private final Pool<Kryo> kryoPool;

    /**
     * Erstellt ein neues {@link KryoCodecSupport} Object.
     *
     * @param kryoPool {@link Pool}<Kryo>
     */
    protected KryoCodecSupport(final Pool<Kryo> kryoPool)
    {
        super();

        this.kryoPool = Objects.requireNonNull(kryoPool, "kryoPool required");
    }

    /**
     * @return {@link Pool}<Kryo>
     */
    protected Pool<Kryo> getKryoPool()
    {
        return this.kryoPool;
    }

    /**
     * @param mimeType {@link MimeType}
     * @return boolean
     */
    protected boolean supportsMimeType(final MimeType mimeType)
    {
        return ((mimeType == null) || MIME_TYPES.stream().anyMatch(m -> m.isCompatibleWith(mimeType)));
    }
}
