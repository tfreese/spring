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
        ReflectionControllerApi api = new ClientReflectionController("http://localhost:65432", ConnectType.HTTP_CONNECTION);
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
     * @param connectType {@link ConnectType}
     */
    @SuppressWarnings("javadoc")
    public ClientReflectionController(final String rootUri, final ConnectType connectType)
    {
        super(rootUri);

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
