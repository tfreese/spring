// Created: 30.01.2020
package de.freese.spring.kryo.reflection.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import de.freese.spring.kryo.reflection.ReflectionControllerApi;
import de.freese.spring.kryo.web.KryoHttpMessageConverter;

/**
 * @param <T> Konkreter Klassentyp der Fassade.
 *
 * @author Thomas Freese
 */
public abstract class AbstractClientReflectionController<T> {
    /**
     * @author Thomas Freese
     */
    public enum ConnectType {
        HTTP_CONNECTION,
        REST_TEMPLATE
    }

    private final Class<T> fassadeType;
    private final Pool<Kryo> kryoPool;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String rootUri;

    @SuppressWarnings("unchecked")
    protected AbstractClientReflectionController(final Pool<Kryo> kryoPool, final String rootUri) {
        super();

        this.kryoPool = Objects.requireNonNull(kryoPool, "kryoPool required");
        this.rootUri = Objects.requireNonNull(rootUri, "rootUri required");

        fassadeType = (Class<T>) ((ParameterizedType) (getClass().getGenericSuperclass())).getActualTypeArguments()[0];
    }

    protected Class<T> getFassadeType() {
        return fassadeType;
    }

    protected Pool<Kryo> getKryoPool() {
        return kryoPool;
    }

    protected Logger getLogger() {
        return logger;
    }

    protected T lookupProxyHttpConnection() {
        return lookupProxyHttpConnection(getFassadeType());
    }

    protected T lookupProxyHttpConnection(final Class<T> fassadeType) {
        final Object proxyObject = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{fassadeType}, (proxy, method, args) -> {

            final URI uri = URI.create(rootUri + "/reflection/" + fassadeType.getSimpleName() + "/" + method.getName());
            final HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

            final Kryo kryo = getKryoPool().obtain();
            final int chunkSize = 1024 * 1024;
            Object result = null;

            try {
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", KryoHttpMessageConverter.APPLICATION_KRYO_VALUE);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setChunkedStreamingMode(chunkSize);

                // Streams berücksichtigen, muss der letzte Parameter sein !
                final boolean hasInputStreamArg = args != null && args.length > 0 && args[args.length - 1] instanceof InputStream;
                final boolean hasOutputStreamArg = args != null && args.length > 0 && args[args.length - 1] instanceof OutputStream;

                connection.setRequestProperty(ReflectionControllerApi.INPUTSTREAM_IN_METHOD, Boolean.toString(hasInputStreamArg));
                connection.setRequestProperty(ReflectionControllerApi.OUTPUTSTREAM_IN_METHOD, Boolean.toString(hasOutputStreamArg));

                connection.connect();

                try (OutputStream outputStream = connection.getOutputStream();
                     Output output = new Output(outputStream, chunkSize)) {
                    if (hasInputStreamArg) {
                        // Parameter-Typen und -Argumente zuerst.
                        kryo.writeClassAndObject(output, new Object[]{method.getParameterTypes(), Arrays.copyOfRange(args, 0, args.length - 1)});

                        // Marker für den Start des InputStreams schreiben.
                        kryo.writeClassAndObject(output, output.total());

                        // InputStream der Argumente direkt in den Output schreiben.
                        try (InputStream inputStream = (InputStream) args[args.length - 1]) {
                            inputStream.transferTo(output);
                        }
                    }
                    else if (hasOutputStreamArg) {
                        // Parameter-Typen und -Argumente zuerst.
                        kryo.writeClassAndObject(output, new Object[]{method.getParameterTypes(), Arrays.copyOfRange(args, 0, args.length - 1)});

                        // Das Schreiben des OutputStreams erfolgt später.
                    }
                    else {
                        // Kein Stream vorhanden.
                        kryo.writeClassAndObject(output, new Object[]{method.getParameterTypes(), args});
                    }

                    output.flush();

                    try (InputStream inputStream = connection.getInputStream()) {
                        if (hasOutputStreamArg) {
                            // InputStream des Servers direkt in den OutputStream der Argumente schreiben.
                            inputStream.transferTo(output);
                        }
                        else {
                            // Result vom Server lesen.
                            try (Input input = new Input(inputStream, chunkSize)) {
                                result = kryo.readClassAndObject(input);
                            }
                        }
                    }
                }
            }
            catch (KryoException ex) {
                // Ignore java.io.IOException: Stream is closed
                getLogger().debug(null, ex);
            }
            catch (Exception ex) {
                // getLogger().error("HTTP {} - {}", connection.getResponseCode(), connection.getResponseMessage());
                getLogger().error(uri.toString());
                // getLogger().error(ex.getMessage(), ex);

                throw ex;
            }
            finally {
                getKryoPool().free(kryo);
            }

            return result;
        });

        return fassadeType.cast(proxyObject);
    }

    protected T lookupProxyRestTemplate(final Class<T> fassadeType) {
        final Object proxyObject = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{fassadeType}, (proxy, method, args) -> {

            final RestTemplate restTemplate = new RestTemplateBuilder()
                    .rootUri(rootUri)
                    .interceptors((request, body, execution) -> {
                        final HttpHeaders headers = request.getHeaders();
                        headers.setAccept(Arrays.asList(KryoHttpMessageConverter.APPLICATION_KRYO));
                        headers.setContentType(KryoHttpMessageConverter.APPLICATION_KRYO);
                        return execution.execute(request, body);
                    })
                    .additionalMessageConverters(new KryoHttpMessageConverter(getKryoPool()), new MappingJackson2HttpMessageConverter())
                    .build();

            // final String url = "/reflection/" + fassadeType.getSimpleName() + "/" + method.getName();
            final String url = "/reflection/" + fassadeType.getSimpleName() + "/rt/" + method.getName();

            try {
                Object[] arguments = args;

                if (arguments == null) {
                    arguments = new Object[0];
                }

                final Class<?>[] paramTypes = new Class<?>[arguments.length];

                final Object[] paramTypesAndArgs = new Object[2];
                paramTypesAndArgs[0] = paramTypes;
                paramTypesAndArgs[1] = arguments;

                for (int i = 0; i < arguments.length; i++) {
                    final Object arg = arguments[i];

                    if (arg == null) {
                        continue;
                    }

                    paramTypes[i] = arg.getClass();
                }

                return restTemplate.postForObject(url, paramTypesAndArgs, Object.class);
            }
            catch (Exception ex) {
                getLogger().error(url);
                // getLogger().error(ex.getMessage(), ex);

                throw ex;
            }
        });

        return fassadeType.cast(proxyObject);
    }

    protected T lookupProxyRetry(final T fassade) {
        return lookupProxyRetry(fassade, getFassadeType());
    }

    @SuppressWarnings("unchecked")
    protected T lookupProxyRetry(final T fassade, final Class<T> fassadeType) {
        // final int timeout = 5000;
        final int maxTrys = 3;
        final int retryDelay = 3000;

        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{fassadeType}, new InvocationHandler() {
            private int invocationCount;

            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                // invocationTime = System.currentTimeMillis();

                return invoke(fassadeType, method, args);
            }

            private Object invoke(final Class<T> fassadeType, final Method method, final Object[] args) throws Throwable {
                try {
                    return method.invoke(fassade, args);
                }
                catch (InvocationTargetException ex) {
                    Throwable cause = ex.getCause();

                    if (cause instanceof UndeclaredThrowableException) {
                        cause = cause.getCause();
                    }

                    // if (((cause instanceof ConnectException) || (cause instanceof IOException) || (cause instanceof SocketException)
                    // || (cause instanceof IllegalAccessException)))
                    // {
                    // && ((System.currentTimeMillis() - invocationTime) < timeout)
                    if (invocationCount < maxTrys) {
                        getLogger().warn("Retry: ({}/{}) {}.{}", invocationCount, maxTrys, fassadeType.getSimpleName(), method.getName());

                        invocationCount++;
                        TimeUnit.MILLISECONDS.sleep(retryDelay);

                        return invoke(fassadeType, method, args);
                    }

                    getLogger().error("Retry failed: {}.{}", fassadeType.getSimpleName(), method.getName());
                    throw cause;
                }
            }
        });
    }
}
