package de.freese.spring.kryo;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.util.Pool;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import de.freese.spring.kryo.web.KryoHttpMessageConverter;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public class KryoApplication implements WebMvcConfigurer {
    public static final Pool<Kryo> KRYO_POOL = new Pool<>(true, true) {
        @Override
        protected Kryo create() {
            Kryo kryo = new Kryo();

            kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
            kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
            kryo.setReferences(true); // Avoid Recursion.
            kryo.setRegistrationRequired(false);
            kryo.setOptimizedGenerics(false);

            kryo.register(Timestamp.class, new TimestampSerializer());
            //            kryo.register(LinkedHashMap.class, new MapSerializer<>());

            // de.javakaffee Serializer's does not work anymore.
            //            kryo.register(Date.class, new de.javakaffee.kryoserializers.DateSerializer(Date.class));
            //            kryo.register(GregorianCalendar.class, new de.javakaffee.kryoserializers.GregorianCalendarSerializer());
            kryo.register(UUID.class, new DefaultSerializers.UUIDSerializer());

            // Supports different JRE Versions.
            SerializerFactory.CompatibleFieldSerializerFactory serializerFactory = new SerializerFactory.CompatibleFieldSerializerFactory();
            serializerFactory.getConfig().setExtendedFieldNames(true);
            kryo.setDefaultSerializer(serializerFactory);

            return kryo;
        }
    };

    public static void main(final String[] args) {
        new SpringApplicationBuilder(KryoApplication.class).run(args);
    }

    @Override
    public void extendMessageConverters(final List<HttpMessageConverter<?>> converters) {
        converters.add(new KryoHttpMessageConverter(KRYO_POOL));
    }

    @Bean
    public Pool<Kryo> kryoPool() {
        return KRYO_POOL;
    }
}
