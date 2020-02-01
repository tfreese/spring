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
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        ReflectionControllerApi api = new ClientReflectionController("http://localhost:65432", CONNECT_TYPE.HTTP_CONNECTION);
        System.out.println(api.testKryo());
    }

    /**
     *
     */
    private final ReflectionControllerApi proxy;

    /**
     * Erstellt ein neues {@link ClientReflectionController} Object.
     *
     * @param rootUri String
     * @param connectType {@link CONNECT_TYPE}
     */
    public ClientReflectionController(final String rootUri, final CONNECT_TYPE connectType)
    {
        super(rootUri);

        if (CONNECT_TYPE.HTTP_CONNECTION.equals(connectType))
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
