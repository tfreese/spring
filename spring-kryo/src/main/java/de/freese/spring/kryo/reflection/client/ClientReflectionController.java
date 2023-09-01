// Created: 30.01.2020
package de.freese.spring.kryo.reflection.client;

import java.time.LocalDateTime;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.Pool;

import de.freese.spring.kryo.reflection.ReflectionControllerApi;

/**
 * @author Thomas Freese
 */
public class ClientReflectionController extends AbstractClientReflectionController<ReflectionControllerApi> implements ReflectionControllerApi {
    private final ReflectionControllerApi proxy;

    public ClientReflectionController(final Pool<Kryo> kryoPool, final String rootUri, final ConnectType connectType) {
        super(kryoPool, rootUri);

        if (ConnectType.HTTP_CONNECTION.equals(connectType)) {
            this.proxy = lookupProxyRetry(lookupProxyHttpConnection());
        }
        else {
            this.proxy = lookupProxyRestTemplate(getFassadeType());
        }
    }

    @Override
    public LocalDateTime testKryo() {
        return this.proxy.testKryo();
    }
}
