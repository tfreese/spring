package de.freese.spring.kryo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;

// @SuppressWarnings("ALL")
public final class KryoRegistrationClasses {
    private static final class KryoRegistrationClassesHolder {
        private static final KryoRegistrationClasses INSTANCE = new KryoRegistrationClasses();

        private KryoRegistrationClassesHolder() {
            super();
        }
    }

    public static KryoRegistrationClasses getInstance() {
        return KryoRegistrationClassesHolder.INSTANCE;
    }

    /**
     * For every Class add the Array-Variant.<br>
     * Integer -> Integer[]
     */
    private static Set<Class<?>> extendWithArrayVariants(final Set<Class<?>> clazzes) {
        final Set<Class<?>> set = HashSet.newHashSet(clazzes.size() * 2);

        for (Class<?> clazz : clazzes) {
            set.add(clazz);

            if (!clazz.isArray()) {
                set.add(clazz.arrayType());
            }
        }

        return set;
    }

    /**
     * All other Classes of the Application.<br>
     * Alphabetic by Class name!
     */
    private static Set<Class<?>> getAppClasses() {
        // Module: base-core
        // return de.freese.base.utils.ClassUtils.getClasses("de.my.package");
        return Set.of();
    }

    /**
     * All other Classes of the Runtime.<br>
     * Alphabetic by Class name!
     */
    private static Set<Class<?>> getRuntimeClasses() {
        return Set.of(
                java.util.ArrayList.class,
                java.util.Arrays.asList().getClass(), // java.util.Arrays.ArrayList
                java.math.BigDecimal.class,
                java.math.BigInteger.class,
                java.util.BitSet.class,
                java.lang.Boolean.class,
                boolean.class,
                java.util.Calendar.class,
                java.lang.Class.class,
                java.util.Collection.class,
                java.util.Date.class,
                java.sql.Date.class,
                java.lang.Double.class,
                double.class,
                java.util.Collections.emptyList().getClass(), // java.util.Collections.EmptyList
                java.util.Collections.emptyMap().getClass(), // java.util.Collections.EmptyMap
                java.util.Collections.emptySet().getClass(), // java.util.Collections.EmptySet
                java.util.EnumMap.class,
                java.lang.Error.class,
                java.lang.Exception.class,
                java.lang.Float.class,
                float.class,
                java.util.GregorianCalendar.class,
                java.util.HashMap.class,
                java.util.HashSet.class,
                // org.apache.commons.lang3.tuple.ImmutablePair.class,
                // org.apache.commons.lang3.tuple.ImmutableTriple.class,
                java.io.InputStream.class,
                java.lang.Integer.class,
                int.class,
                com.esotericsoftware.kryo.KryoException.class,
                com.esotericsoftware.kryo.io.KryoBufferOverflowException.class,
                com.esotericsoftware.kryo.io.KryoBufferUnderflowException.class,
                java.util.LinkedHashMap.class,
                java.util.LinkedHashSet.class,
                java.util.List.class,
                java.util.List.of(1).getClass(), // java.util.ImmutableCollections.List12
                java.util.List.of(1, 2, 3, 4).getClass(), // java.util.ImmutableCollections.ListN
                java.time.LocalDate.class,
                java.time.LocalDateTime.class,
                java.lang.Long.class,
                long.class,
                java.util.Map.class,
                java.lang.Object.class,
                java.io.OutputStream.class,
                // org.apache.commons.lang3.tuple.Pair.class,
                java.lang.RuntimeException.class,
                java.util.Set.class,
                java.util.SortedMap.class,
                java.util.SortedSet.class,
                java.lang.StackTraceElement.class,
                java.lang.String.class,
                java.lang.StringBuffer.class,
                java.lang.StringBuilder.class,
                java.sql.Timestamp.class,
                java.util.TreeMap.class,
                java.util.TreeSet.class,
                // org.apache.commons.lang3.tuple.Triple.class
                java.util.UUID.class
        );
    }

    private final Map<Class<?>, Serializer<?>> serializers = new HashMap<>();
    private Set<Class<?>> registrationClasses;

    private KryoRegistrationClasses() {
        super();

        serializers.put(java.sql.Timestamp.class, new TimestampSerializer());
        serializers.put(java.util.UUID.class, new DefaultSerializers.UUIDSerializer());

        // de.javakaffee Serializer's does not work anymore.
        // serializers.put(java.util.EnumMap.class, new de.javakaffee.kryoserializers.EnumMapSerializer());
        // serializers.put(Date.class, new de.javakaffee.kryoserializers.DateSerializer(Date.class));
        // serializers.put(GregorianCalendar.class, new de.javakaffee.kryoserializers.GregorianCalendarSerializer());
    }

    /**
     * Without Class-Registration only add the defined Serializer.
     */
    public synchronized void registerClasses(final Kryo kryo, final boolean registerClasses) {
        if (!registerClasses) {
            serializers.forEach(kryo::addDefaultSerializer);

            return;
        }

        if (registrationClasses == null) {
            Set<Class<?>> clazzes = HashSet.newHashSet(512);
            // clazzes.addAll(ReflectionUtils.getClasses("..."));

            /// spring-context
            // ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
            // provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
            // provider.findCandidateComponents("de.freese").forEach(beanDef -> System.out.println(beanDef.getBeanClassName()));

            clazzes.addAll(getAppClasses());
            clazzes.addAll(getRuntimeClasses());

            // Register Array-Variants of the classes.
            clazzes = extendWithArrayVariants(clazzes);

            // Sorting is mandatory, each Class must have the same Registration-ID on Client and Server.
            registrationClasses = clazzes.stream().sorted(Comparator.comparing(Class::getName)).collect(Collectors.toCollection(LinkedHashSet::new));
        }

        registrationClasses.forEach(clazz -> {
            final Serializer<?> serializer = serializers.get(clazz);

            if (serializer == null) {
                kryo.register(clazz, kryo.getNextRegistrationId());
            }
            else {
                kryo.register(clazz, serializer, kryo.getNextRegistrationId());
            }
        });
    }
}
