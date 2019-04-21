package com.baeldung.rsocket;

import static com.baeldung.rsocket.support.Constants.TCP_PORT;
import com.baeldung.rsocket.support.GameController;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import reactor.core.publisher.Flux;

/**
 * @author Thomas Freese
 */
public class ChannelClient
{
    /**
     *
     */
    private final GameController gameController;

    /**
     *
     */
    private final RSocket socket;

    /**
     * Erstellt ein neues {@link ChannelClient} Object.
     */
    public ChannelClient()
    {
        super();

        // @formatter:off
        this.socket = RSocketFactory
                .connect()
                .transport(TcpClientTransport.create("localhost", TCP_PORT))
                .start()
                .block();
        // @formatter:on

        this.gameController = new GameController("Client Player");
    }

    /**
     *
     */
    public void dispose()
    {
        this.socket.dispose();
    }

    /**
     *
     */
    public void playGame()
    {
        // @formatter:off
        this.socket.requestChannel(Flux.from(this.gameController))
            .doOnNext(this.gameController::processPayload)
            .blockLast();
        // @formatter:on
    }
}
