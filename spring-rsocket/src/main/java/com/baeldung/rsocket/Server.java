package com.baeldung.rsocket;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baeldung.rsocket.support.Constants;
import com.baeldung.rsocket.support.DataPublisher;
import com.baeldung.rsocket.support.GameController;
import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.TcpServerTransport;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
public class Server
{
    /**
     * RSocket implementation
     */
    private class RSocketImpl extends AbstractRSocket
    {
        /**
         * Handle Fire-and-Forget messages.
         *
         * @param payload Message {@link Payload}
         * @return {@link Mono}
         * @see io.rsocket.AbstractRSocket#fireAndForget(io.rsocket.Payload)
         */
        @Override
        public Mono<Void> fireAndForget(final Payload payload)
        {
            try
            {
                Server.this.dataPublisher.publish(payload); // forward the payload

                return Mono.empty();
            }
            catch (Exception ex)
            {
                return Mono.error(ex);
            }
        }

        /**
         * Handle request for bidirectional channel.
         *
         * @param payloads Stream of payloads delivered from the client
         * @return {@link Flux}
         * @see io.rsocket.AbstractRSocket#requestChannel(org.reactivestreams.Publisher)
         */
        @Override
        public Flux<Payload> requestChannel(final Publisher<Payload> payloads)
        {
            Flux.from(payloads).subscribe(Server.this.gameController::processPayload);
            Flux<Payload> channel = Flux.from(Server.this.gameController);

            return channel;
        }

        /**
         * Handle Request/Response messages.
         *
         * @param payload Message payload
         * @return payload response
         * @see io.rsocket.AbstractRSocket#requestResponse(io.rsocket.Payload)
         */
        @Override
        public Mono<Payload> requestResponse(final Payload payload)
        {
            try
            {
                return Mono.just(payload); // reflect the payload back to the sender
            }
            catch (Exception ex)
            {
                return Mono.error(ex);
            }
        }

        /**
         * Handle Request/Stream messages. Each request returns a new stream.
         *
         * @param payload Payload that can be used to determine which stream to return
         * @return Flux stream containing simulated measurement data
         * @see io.rsocket.AbstractRSocket#requestStream(io.rsocket.Payload)
         */
        @Override
        public Flux<Payload> requestStream(final Payload payload)
        {
            String streamName = payload.getDataUtf8();

            if (Constants.DATA_STREAM_NAME.equals(streamName))
            {
                return Flux.from(Server.this.dataPublisher);
            }

            return Flux.error(new IllegalArgumentException(streamName));
        }
    }

    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    /**
     * @return {@link Logger}
     */
    private static Logger getLogger()
    {
        return LOGGER;
    }

    /**
     *
     */
    private final DataPublisher dataPublisher = new DataPublisher();

    /**
     *
     */
    private final GameController gameController;

    /**
     *
     */
    private final Disposable server;

    /**
     * Erstellt ein neues {@link Server} Object.
     */
    public Server()
    {
        super();

        // @formatter:off
        this.server = RSocketFactory
                .receive()
                .acceptor((setupPayload, reactiveSocket) -> Mono.just(new RSocketImpl()))
                .transport(TcpServerTransport.create("localhost", Constants.TCP_PORT))
                .start()
                .doOnNext(x -> getLogger().info("Server started"))
                .subscribe();
        // @formatter:on

        this.gameController = new GameController("Server Player");
    }

    /**
     *
     */
    public void dispose()
    {
        this.dataPublisher.complete();
        this.server.dispose();
    }
}
