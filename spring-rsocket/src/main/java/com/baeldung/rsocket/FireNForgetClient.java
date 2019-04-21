package com.baeldung.rsocket;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
public class FireNForgetClient
{
    /**
     *
     */
    private final List<Float> data;

    /**
     *
     */
    private final RSocket socket;

    /**
     * Erstellt ein neues {@link FireNForgetClient} Object.
     */
    public FireNForgetClient()
    {
        super();

        // @formatter:off
        this.socket = RSocketFactory
                .connect()
                .transport(TcpClientTransport.create("localhost", Constants.TCP_PORT))
                .start()
                .block();
        // @formatter:on

        this.data = Collections.unmodifiableList(generateData());
    }

    /**
     * Create a binary payload containing a single float value.
     *
     * @param index Index into the data list
     * @return Payload ready to send to the server
     */
    private Payload createFloatPayload(final Long index)
    {
        float velocity = this.data.get(index.intValue());
        ByteBuffer buffer = ByteBuffer.allocate(4).putFloat(velocity);
        buffer.rewind();

        return DefaultPayload.create(buffer);
    }

    /**
     *
     */
    public void dispose()
    {
        this.socket.dispose();
    }

    /**
     * Generate sample data.
     *
     * @return List of random floats
     */
    private List<Float> generateData()
    {
        List<Float> dataList = new ArrayList<>(Constants.DATA_LENGTH);
        float velocity = 0;

        for (int i = 0; i < Constants.DATA_LENGTH; i++)
        {
            velocity += Math.random();
            dataList.add(velocity);
        }

        return dataList;
    }

    /**
     * Get the data used for this client.
     *
     * @return list of data values
     */
    public List<Float> getData()
    {
        return this.data;
    }

    /**
     * Send binary velocity (float) every 50ms.
     */
    public void sendData()
    {
        // @formatter:off
        Flux.interval(Duration.ofMillis(50))
            .take(this.data.size())
            .map(this::createFloatPayload)
            .flatMap(this.socket::fireAndForget)
            .blockLast();
        // @formatter:on
    }
}
