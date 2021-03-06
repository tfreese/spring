/**
 * Created: 22.05.2018
 */

package de.freese.spring.kryo.reflection;

import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.Pool;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("reflection/ReflectionControllerApi")
public class RestKryoReflectionController extends AbstractRestReflectionController implements ReflectionControllerApi
{
    /**
     * Erstellt ein neues {@link RestKryoReflectionController} Object.
     *
     * @param kryoPool {@link Pool}<Kryo>
     */
    public RestKryoReflectionController(final Pool<Kryo> kryoPool)
    {
        super(kryoPool);
    }

    /**
     * @see de.freese.spring.kryo.reflection.ReflectionControllerApi#testKryo()
     */
    @Override
    public LocalDateTime testKryo()
    {
        LocalDateTime localDateTime = LocalDateTime.now();

        return localDateTime;
    }
}
