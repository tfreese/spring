/**
 * Created: 30.01.2020
 */

package de.freese.spring.kryo.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import de.freese.spring.kryo.KryoApplication;

/**
 * @author Thomas Freese
 */
public abstract class AbstractRestReflectionController
{
    /**
     * Erstellt ein neues {@link AbstractRestReflectionController} Object.
     */
    public AbstractRestReflectionController()
    {
        super();
    }

    /**
     * @param arguments Object[]
     * @param argument Object
     * @return Object[]
     */
    protected Object[] addArgument(final Object[] arguments, final Object argument)
    {
        Object[] newArgs = null;

        if (arguments == null)
        {
            newArgs = new Object[0];
        }

        Arrays.copyOf(newArgs, newArgs.length + 1);

        newArgs[newArgs.length - 1] = argument;

        return newArgs;
    }

    /**
     * @param method final
     * @param request {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @return Object
     * @throws Exception Falls was schief geht.
     */
    @PostMapping(path = "{method}", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public Object invoke(@PathVariable("method") final String method, final HttpServletRequest request, final HttpServletResponse response) throws Exception
    {
        Kryo kryo = KryoApplication.KRYO_SERIALIZER.get();

        try (Input inputStream = new Input(request.getInputStream(), 1024 * 1024))
        {
            // Parameter-Typen und -Argumente auslesen.
            Object[] paramTypesAndArgs = (Object[]) kryo.readClassAndObject(inputStream);
            Class<?>[] parameterTypes = (Class<?>[]) paramTypesAndArgs[0];
            Object[] arguments = (Object[]) paramTypesAndArgs[1];

            // Streams berücksichtigen.
            if (Boolean.valueOf(request.getHeader(ReflectionControllerApi.INPUTSTREAM_IN_METHOD)))
            {
                // Marker zwischen ParameterTypes/Argumenten und InputStream lesen.
                kryo.readClassAndObject(inputStream);

                // InputStream als letzten Parameter den Argumenten hinzufügen.
                arguments = addArgument(arguments, inputStream);
            }
            else if (Boolean.valueOf(request.getHeader(ReflectionControllerApi.OUTPUTSTREAM_IN_METHOD)))
            {
                // OutputStream als letzten Parameter den Argumenten hinzufügen.
                arguments = addArgument(arguments, response.getOutputStream());
            }

            // Konkrete Methode aufrufen.
            Method apiMethod = getClass().getMethod(method, parameterTypes);
            Object result = apiMethod.invoke(this, arguments);

            // Ergebnis mit Kryo codieren.
            try (Output output = new Output(response.getOutputStream()))
            {
                kryo.writeClassAndObject(output, result);
                output.flush();
            }
        }
        catch (Exception ex)
        {
            if (ex instanceof InvocationTargetException)
            {
                ex = (Exception) ex.getCause();
            }

            // if(ex instanceof ...)
            // {
            // ex = ex.getCause();
            // }

            throw ex;
        }

        return null;
    }
}
