/**
 * Created: 29.01.2020
 */

package de.freese.spring.kryo.webflux;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import com.esotericsoftware.kryo.Kryo;
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
    private final Supplier<Kryo> supplier;

    /**
     * Erstellt ein neues {@link KryoCodecSupport} Object.
     * 
     * @param supplier {@link Supplier}
     */
    public KryoCodecSupport(final Supplier<Kryo> supplier)
    {
        super();

        this.supplier = Objects.requireNonNull(supplier, "kryo supplier required");
    }

    /**
     * @return {@link Kryo}
     */
    protected Kryo getKryo()
    {
        Kryo kryo = this.supplier.get();

        return kryo;
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
