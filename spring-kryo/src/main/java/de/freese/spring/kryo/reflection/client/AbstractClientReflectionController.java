/**
 * Created: 30.01.2020
 */

package de.freese.spring.kryo.reflection.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import de.freese.spring.kryo.KryoApplication;
import de.freese.spring.kryo.reflection.ReflectionControllerApi;

/**
 * @author Thomas Freese
 * @param <T> Konkrete Klasse der Fassade.
 */
public abstract class AbstractClientReflectionController<T>
{
    /**
     *
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     *
     */
    private final String rootUri;

    /**
     * Erstellt ein neues {@link AbstractClientReflectionController} Object.
     *
     * @param rootUri String
     */
    public AbstractClientReflectionController(final String rootUri)
    {
        super();

        this.rootUri = Objects.requireNonNull(rootUri, "rootUri required");
    }

    /**
     * @return {@link Logger}
     */
    protected Logger getLogger()
    {
        return this.logger;
    }

    /**
     * @param fassade Class
     * @return Object
     */
    @SuppressWarnings("unchecked")
    protected T lookupHttpConnection(final Class<T> fassade)
    {
        return (T) Proxy.newProxyInstance(fassade.getClassLoader(), new Class[]
        {
                fassade
        }, (proxy, method, args) -> {
            Kryo kryo = KryoApplication.KRYO_SERIALIZER.get();
            int chunkSize = 1024 * 1024;

            HttpURLConnection connection =
                    (HttpURLConnection) new URL(this.rootUri + "/reflection/" + fassade.getSimpleName() + "/" + method.getName()).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setChunkedStreamingMode(chunkSize);

            // Streams berücksichtigen, muss der letzte Parameter sein !
            boolean hasInputStreamArg = (args != null) && (args.length > 0) && (args[args.length - 1] instanceof InputStream);
            boolean hasOutputStreamArg = (args != null) && (args.length > 0) && (args[args.length - 1] instanceof OutputStream);

            connection.setRequestProperty(ReflectionControllerApi.INPUTSTREAM_IN_METHOD, Boolean.toString(hasInputStreamArg));
            connection.setRequestProperty(ReflectionControllerApi.OUTPUTSTREAM_IN_METHOD, Boolean.toString(hasOutputStreamArg));

            connection.connect();

            Object result = null;

            try (OutputStream outputStream = connection.getOutputStream();
                 Output output = new Output(outputStream, chunkSize))
            {
                if (hasInputStreamArg)
                {
                    // Parameter-Typen und -Argumente zuerst.
                    kryo.writeClassAndObject(output, new Object[]
                    {
                            method.getParameterTypes(), Arrays.copyOfRange(args, 0, args.length - 1)
                    });

                    // Marker für den Start des InputStreams schreiben.
                    kryo.writeClassAndObject(output, output.total());

                    // InputStream der Argumente direkt in den Output schreiben.
                    try (InputStream inputStream = (InputStream) args[args.length - 1])
                    {
                        inputStream.transferTo(output);
                    }
                }
                else if (hasOutputStreamArg)
                {
                    // Parameter-Typen und -Argumente zuerst.
                    kryo.writeClassAndObject(output, new Object[]
                    {
                            method.getParameterTypes(), Arrays.copyOfRange(args, 0, args.length - 1)
                    });

                    // Das Schreiben des OutputStreams erfolgt später.
                }
                else
                {
                    // Kein Stream vorhanden.
                    kryo.writeClassAndObject(output, new Object[]
                    {
                            method.getParameterTypes(), args
                    });
                }

                output.flush();

                try (InputStream inputStream = connection.getInputStream())
                {
                    if (hasOutputStreamArg)
                    {
                        // InputStream des Servers direkt in den OutputStream der Argumente schreiben.
                        inputStream.transferTo(output);
                    }
                    else
                    {
                        // Result vom Server lesen.
                        try (Input input = new Input(inputStream, chunkSize))
                        {
                            result = kryo.readClassAndObject(input);
                        }
                    }
                }
            }
            catch (KryoException ex)
            {
                // Ignore java.io.IOException: Stream is closed
                getLogger().debug(null, ex);
            }
            catch (Exception ex)
            {
                getLogger().error("HTTP {} - {}", connection.getResponseCode(), connection.getResponseMessage());
                getLogger().error(null, ex);

                throw ex;
            }

            return result;
        });
    }
}
