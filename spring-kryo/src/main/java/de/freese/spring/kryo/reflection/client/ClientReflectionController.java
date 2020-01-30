/**
 * Created: 30.01.2020
 */

package de.freese.spring.kryo.reflection.client;

import java.time.LocalDateTime;
import de.freese.spring.kryo.reflection.ReflectionControllerApi;

/**
 * @author Thomas Freese
 */
public class ClientReflectionController extends AbstractClientReflectionController<ReflectionControllerApi> implements ReflectionControllerApi
{
    /**
     *
     */
    private final ReflectionControllerApi proxy;

    /**
     * Erstellt ein neues {@link ClientReflectionController} Object.
     *
     * @param rootUri String
     */
    public ClientReflectionController(final String rootUri)
    {
        super(rootUri);

        this.proxy = lookupHttpConnection(ReflectionControllerApi.class);
    }

    /**
     * @see de.freese.spring.kryo.reflection.ReflectionControllerApi#testKryo()
     */
    @Override
    public LocalDateTime testKryo()
    {
        return this.proxy.testKryo();
    }
}
