// Created: 19.02.2026
package de.freese.spring.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.util.MapReferenceResolver;
import com.esotericsoftware.kryo.util.Pool;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * @author Thomas Freese
 */
public class KryoPool {
    public static final Pool<Kryo> KRYO_POOL = new Pool<>(true, true) {
        @Override
        protected Kryo create() {
            final Kryo kryo = new Kryo();

            // kryo.setClassLoader(Thread.currentThread().getContextClassLoader()); // Must be set by every usage from Pool!
            kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
            kryo.setReferences(true); // Avoid Recursion.
            kryo.setOptimizedGenerics(false);
            kryo.setReferenceResolver(new MapReferenceResolver() {
                @Override
                @SuppressWarnings("rawtypes")
                public boolean useReferences(final Class type) {
                    return super.useReferences(type) && !String.class.equals(type); // For Problems with String References.
                }
            });

            final boolean registerClasses = false;

            kryo.setRegistrationRequired(registerClasses);
            kryo.setWarnUnregisteredClasses(registerClasses);

            KryoRegistrationClasses.registerClasses(kryo, registerClasses);

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

    public Kryo getKryo() {
        final Kryo kryo = KRYO_POOL.obtain();
        kryo.setClassLoader(Thread.currentThread().getContextClassLoader());

        return kryo;
    }

    public void returnKryo(final Kryo kryo) {
        if (kryo == null) {
            return;
        }

        KRYO_POOL.free(kryo);
    }
}
