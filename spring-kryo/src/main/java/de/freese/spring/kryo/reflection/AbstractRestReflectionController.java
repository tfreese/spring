// Created: 30.01.2020
package de.freese.spring.kryo.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import de.freese.spring.kryo.web.KryoHttpMessageConverter;

/**
 * @author Thomas Freese
 */
public abstract class AbstractRestReflectionController {
    private final Pool<Kryo> kryoPool;

    protected AbstractRestReflectionController(final Pool<Kryo> kryoPool) {
        super();

        this.kryoPool = Objects.requireNonNull(kryoPool, "kryoPool required");
    }

    @PostMapping(path = "{method}", consumes = KryoHttpMessageConverter.APPLICATION_KRYO_VALUE, produces = KryoHttpMessageConverter.APPLICATION_KRYO_VALUE)
    public Object invoke(@PathVariable("method") final String method, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final Kryo kryo = getKryoPool().obtain();

        try (Input inputStream = new Input(request.getInputStream(), 1024 * 1024)) {
            // Parameter-Typen und -Argumente auslesen.
            final Object[] paramTypesAndArgs = (Object[]) kryo.readClassAndObject(inputStream);
            final Class<?>[] parameterTypes = (Class<?>[]) paramTypesAndArgs[0];
            Object[] arguments = (Object[]) paramTypesAndArgs[1];

            // Streams berücksichtigen.
            if (Boolean.parseBoolean(request.getHeader(ReflectionControllerApi.INPUTSTREAM_IN_METHOD))) {
                // Marker zwischen ParameterTypes/Argumenten und InputStream lesen.
                kryo.readClassAndObject(inputStream);

                // InputStream als letzten Parameter den Argumenten hinzufügen.
                arguments = addArgument(arguments, inputStream);
            }
            else if (Boolean.parseBoolean(request.getHeader(ReflectionControllerApi.OUTPUTSTREAM_IN_METHOD))) {
                // OutputStream als letzten Parameter den Argumenten hinzufügen.
                arguments = addArgument(arguments, response.getOutputStream());
            }

            // Konkrete Methode aufrufen.
            final Method apiMethod = getClass().getMethod(method, parameterTypes);
            final Object result = apiMethod.invoke(this, arguments);

            // Ergebnis mit Kryo codieren.
            try (Output output = new Output(response.getOutputStream())) {
                kryo.writeClassAndObject(output, result);
                output.flush();
            }
        }
        catch (InvocationTargetException ex) {
            throw (Exception) ex.getCause();
        }
        catch (Exception ex) {
            throw ex;
        }
        finally {
            getKryoPool().free(kryo);
        }

        return null;
    }

    /**
     * Funktioniert nur mit {@link RestTemplate}.
     */
    @PostMapping(path = "/rt/{method}", consumes = KryoHttpMessageConverter.APPLICATION_KRYO_VALUE, produces = KryoHttpMessageConverter.APPLICATION_KRYO_VALUE)
    public Object invokeFromRestTemplate(@PathVariable("method") final String method, @RequestBody final Object body) throws Exception {
        // Parameter-Typen und -Argumente auslesen.
        final Object[] paramTypesAndArgs = (Object[]) body;
        final Class<?>[] parameterTypes = (Class<?>[]) paramTypesAndArgs[0];
        final Object[] arguments = (Object[]) paramTypesAndArgs[1];

        // Konkrete Methode aufrufen.
        final Method apiMethod = getClass().getMethod(method, parameterTypes);

        return apiMethod.invoke(this, arguments);
    }

    protected Object[] addArgument(final Object[] arguments, final Object argument) {
        Object[] newArgs = arguments != null ? arguments : new Object[0];

        newArgs = Arrays.copyOf(newArgs, newArgs.length + 1);

        newArgs[newArgs.length - 1] = argument;

        return newArgs;
    }

    protected Pool<Kryo> getKryoPool() {
        return this.kryoPool;
    }
}
