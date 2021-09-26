// Created: 30.01.2020
package de.freese.spring.kryo.reflection.client;

import java.time.LocalDateTime;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.Pool;

import de.freese.spring.kryo.KryoApplication;
import de.freese.spring.kryo.reflection.ReflectionControllerApi;

/**
 * @author Thomas Freese
 */
public class ClientReflectionController extends AbstractClientReflectionController<ReflectionControllerApi> implements ReflectionControllerApi
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        ReflectionControllerApi api = new ClientReflectionController(KryoApplication.KRYO_POOL, "http://localhost:65432", ConnectType.HTTP_CONNECTION);
        System.out.println(api.testKryo());
    }

    /**
     *
     */
    private final ReflectionControllerApi proxy;

    /**
     * Erstellt ein neues {@link ClientReflectionController} Object.
     *
     * @param kryoPool {@link Pool}<Kryo>
     * @param rootUri String
     * @param connectType {@link ConnectType}
     */
    @SuppressWarnings("javadoc")
    public ClientReflectionController(final Pool<Kryo> kryoPool, final String rootUri, final ConnectType connectType)
    {
        super(kryoPool, rootUri);

        if (ConnectType.HTTP_CONNECTION.equals(connectType))
        {
            this.proxy = lookupProxyRetry(lookupProxyHttpConnection());
        }
        else
        {
            this.proxy = lookupProxyRestTemplate(getFassadeType());
        }
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
