package com.baeldung.rsocket;

import com.baeldung.rsocket.support.Constants;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Flux;

/**
 * @author Thomas Freese
 */
public class ReqStreamClient
{
    /**
     *
     */
    private final RSocket socket;

    /**
     * Erstellt ein neues {@link ReqStreamClient} Object.
     */
    public ReqStreamClient()
    {
        super();

        // @formatter:off
        this.socket = RSocketFactory
                .connect()
                .transport(TcpClientTransport.create("localhost", Constants.TCP_PORT))
                .start()
                .block();
        // @formatter:on
    }

    /**
     *
     */
    public void dispose()
    {
        this.socket.dispose();
    }

    /**
     * @return {@link Flux}
     */
    public Flux<Float> getDataStream()
    {
        // @formatter:off
        return this.socket.requestStream(DefaultPayload.create(Constants.DATA_STREAM_NAME))
                .map(Payload::getData)
                .map(buf -> buf.getFloat())
                .onErrorReturn(null);
        // @formatter:on
    }
}
