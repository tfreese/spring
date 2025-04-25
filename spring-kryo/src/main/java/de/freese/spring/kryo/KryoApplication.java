package de.freese.spring.kryo;

import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.util.MapReferenceResolver;
import com.esotericsoftware.kryo.util.Pool;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
            final Kryo kryo = new Kryo();

            kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
            kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
            kryo.setReferences(true); // Avoid Recursion.
            kryo.setOptimizedGenerics(false);
            kryo.setReferenceResolver(new MapReferenceResolver() {
                @Override
                public boolean useReferences(final Class type) {
                    return super.useReferences(type) && !String.class.equals(type); // For Problems with String References.
                }
            });

            final boolean registerClasses = false;

            kryo.setRegistrationRequired(registerClasses);
            kryo.setWarnUnregisteredClasses(registerClasses);

            KryoRegistrationClasses.getInstance().registerClasses(kryo, registerClasses);

            // UnmodifiableCollectionsSerializer.registerSerializers(kryo);
            // SynchronizedCollectionsSerializer.registerSerializers(kryo);

            // Supports different JRE Versions and different order of fields.
            final SerializerFactory.CompatibleFieldSerializerFactory serializerFactory = new SerializerFactory.CompatibleFieldSerializerFactory();
            serializerFactory.getConfig().setExtendedFieldNames(true);
            serializerFactory.getConfig().setFieldsAsAccessible(true);
            serializerFactory.getConfig().setReadUnknownFieldData(true);
            // serializerFactory.getConfig().setChunkedEncoding(true);
            kryo.setDefaultSerializer(serializerFactory);

            return kryo;
        }
    };

    public static void main(final String[] args) {
        SpringApplication.run(KryoApplication.class, args);
        // new SpringApplicationBuilder(KryoApplication.class).run(args);
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
