// Created: 17.12.2016
package de.freese.spring.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * The obligatory "Hello World!" showing a simple implementation of a {@link HystrixCommand}.
 *
 * @author Thomas Freese
 */
public class CommandHelloWorld extends HystrixCommand<String> {
    private final String name;

    public CommandHelloWorld(final String name) {
        // CommandGroupKey = ThreadPool-Name
        super(HystrixCommandGroupKey.Factory.asKey("TestGroup"));

        this.name = name;
    }

    /**
     * @see com.netflix.hystrix.HystrixCommand#run()
     */
    @Override
    protected String run() throws Exception {
        return "Hello " + this.name + "!";
    }
}
