package com.baeldung.rsocket;

import com.baeldung.rsocket.support.Constants;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;

/**
 * @author Thomas Freese
 */
public class ReqResClient
{
    /**
     *
     */
    private final RSocket socket;

    /**
     * Erstellt ein neues {@link ReqResClient} Object.
     */
    public ReqResClient()
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
     * @param string String
     * @return String
     */
    public String callBlocking(final String string)
    {
        // @formatter:off
        return this.socket.requestResponse(DefaultPayload.create(string))
                .map(Payload::getDataUtf8)
                .onErrorReturn(Constants.ERROR_MSG)
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
}
