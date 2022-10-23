package de.freese.spring.kryo;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import com.esotericsoftware.kryo.serializers.TimeSerializers.LocalDateTimeSerializer;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.util.Pool;
import de.freese.spring.kryo.web.KryoHttpMessageConverter;
import de.javakaffee.kryoserializers.DateSerializer;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public class KryoApplication implements WebMvcConfigurer
{
    public static final Pool<Kryo> KRYO_POOL = new Pool<>(true, true)
    {
        /**
         * @see com.esotericsoftware.kryo.util.Pool#create()
         */
        @Override
        protected Kryo create()
        {
            Kryo kryo = new Kryo();

            kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
            kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
            kryo.setReferences(true); // Verhindert Rekursion.
            kryo.setRegistrationRequired(false);

            kryo.register(Date.class, new DateSerializer(Date.class));
            kryo.register(Timestamp.class, new TimestampSerializer());

            // Unable to make field private java.util.TimeZone java.util.Calendar.zone accessible
            // kryo.register(GregorianCalendar.class, new GregorianCalendarSerializer());

            kryo.register(LinkedHashMap.class, new MapSerializer<>());
            kryo.register(LocalDateTime.class, new LocalDateTimeSerializer());

            // NoClassDefFoundError
            // UnmodifiableCollectionsSerializer.registerSerializers(kryo);
            // SynchronizedCollectionsSerializer.registerSerializers(kryo);

            return kryo;
        }
    };

    public static void main(final String[] args)
    {
        new SpringApplicationBuilder(KryoApplication.class).run(args);
    }

    /**
     * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#extendMessageConverters(java.util.List)
     */
    @Override
    public void extendMessageConverters(final List<HttpMessageConverter<?>> converters)
    {
        converters.add(new KryoHttpMessageConverter(KRYO_POOL));
    }

    @Bean
    public Pool<Kryo> kryoPool()
    {
        return KRYO_POOL;
    }
}
