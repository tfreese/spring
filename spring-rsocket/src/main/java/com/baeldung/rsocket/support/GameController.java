package com.baeldung.rsocket.support;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.rsocket.Payload;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Flux;

/**
 * @author Thomas Freese
 */
public class GameController implements Publisher<Payload>
{
    /**
     *
     */
    private final static AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

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
    private final String playerName;

    /**
     *
     */
    private final List<Long> shots;

    /**
     *
     */
    private Subscriber<? super Payload> subscriber;

    /**
     *
     */
    private boolean truce = false;

    /**
     * Erstellt ein neues {@link GameController} Object.
     *
     * @param playerName String
     */
    public GameController(final String playerName)
    {
        super();

        this.playerName = Objects.requireNonNull(playerName, "playerName required");
        this.shots = generateShotList();
    }

    /**
     * Publish game events asynchronously.
     */
    private void fireAtWill()
    {
        new Thread(() -> {
            for (Long shotDelay : this.shots)
            {
                try
                {
                    Thread.sleep(shotDelay);
                }
                catch (Exception xx)
                {
                }

                if (this.truce)
                {
                    getLogger().info("{}: truce", this.playerName);
                    break;
                }

                getLogger().info("{}: bang!", this.playerName);
                this.subscriber.onNext(DefaultPayload.create("bang!"));
            }

            if (!this.truce)
            {
                getLogger().info("{}: I give up!", this.playerName);
                this.subscriber.onNext(DefaultPayload.create("I give up"));
            }

            this.subscriber.onComplete();
        }, "GameController_" + ATOMIC_INTEGER.incrementAndGet()).start();
    }

    /**
     * Create a random list of time intervals, 0-1000ms
     *
     * @return List of time intervals
     */
    private List<Long> generateShotList()
    {
        // @formatter:off
        return Flux.range(1, Constants.SHOT_COUNT)
                .map(x -> (long) Math.ceil(Math.random() * 1000))
                .collectList()
                .block();
        // @formatter:on
    }

    /**
     * Process events from the opponent
     *
     * @param payload Payload received from the rSocket
     */
    public void processPayload(final Payload payload)
    {
        String message = payload.getDataUtf8();

        switch (message)
        {
            case "bang!":
                String result = Math.random() < 0.5 ? "Haha missed!" : "Ow!";
                getLogger().info("{}: {}", this.playerName, result);
                break;
            case "I give up":
                this.truce = true;
                getLogger().info("{}: OK, truce", this.playerName);
                break;
            default:
                getLogger().error("Unknown message !");
                break;

        }
    }

    /**
     * @see org.reactivestreams.Publisher#subscribe(org.reactivestreams.Subscriber)
     */
    @Override
    public void subscribe(final Subscriber<? super Payload> subscriber)
    {
        this.subscriber = subscriber;

        fireAtWill();
    }
}
