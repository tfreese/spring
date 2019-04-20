package com.baeldung.rsocket.support;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import io.rsocket.Payload;

/**
 * Simple PUblisher to provide async data to Flux stream.
 */
public class DataPublisher implements Publisher<Payload>
{
    /**
     *
     */
    private Subscriber<? super Payload> subscriber = null;

    /**
     * Erstellt ein neues {@link DataPublisher} Object.
     */
    public DataPublisher()
    {
        super();
    }

    /**
     *
     */
    public void complete()
    {
        if (this.subscriber != null)
        {
            this.subscriber.onComplete();
        }
    }

    /**
     * @param payload {@link Payload}
     */
    public void publish(final Payload payload)
    {
        if (this.subscriber != null)
        {
            this.subscriber.onNext(payload);
        }
    }

    /**
     * @see org.reactivestreams.Publisher#subscribe(org.reactivestreams.Subscriber)
     */
    @Override
    public void subscribe(final Subscriber<? super Payload> subscriber)
    {
        this.subscriber = subscriber;
    }
}
