/**
 * Created: 17.12.2016
 */

package de.freese.spring.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * Sample {@link HystrixCommand} showing a basic fallback implementation.
 *
 * @author Thomas Freese
 */
public class CommandHelloFailure extends HystrixCommand<String>
{
    /**
     *
     */
    private final String name;

    /**
     * Erstellt ein neues {@link CommandHelloFailure} Object.
     *
     * @param name String
     */
    public CommandHelloFailure(final String name)
    {
        // CommandGroupKey = ThreadPool-Name
        super(HystrixCommandGroupKey.Factory.asKey("TestGroup"));

        this.name = name;
    }

    /**
     * @see com.netflix.hystrix.HystrixCommand#getFallback()
     */
    @Override
    protected String getFallback()
    {
        return "Hello Failure " + this.name + "!";
    }

    /**
     * @see com.netflix.hystrix.HystrixCommand#run()
     */
    @Override
    protected String run() throws Exception
    {
        throw new RuntimeException("this command always fails");
    }
}
