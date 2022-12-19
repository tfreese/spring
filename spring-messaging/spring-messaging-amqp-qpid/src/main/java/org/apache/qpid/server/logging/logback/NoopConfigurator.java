// Created: 19.12.22
package org.apache.qpid.server.logging.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * BugFix for Error-Message:<br>
 * <code>
 * Exception in thread "main" java.lang.AbstractMethodError:<br>
 * Receiver class org.apache.qpid.server.logging.logback.NoopConfigurator does not define or inherit an implementation of the resolved method<br>
 * 'abstract ch.qos.logback.classic.spi.Configurator$ExecutionStatus configure(ch.qos.logback.classic.LoggerContext)'<br>
 * of interface ch.qos.logback.classic.spi.Configurator.<br>
 * </code>
 *
 * @author Thomas Freese
 */
public class NoopConfigurator extends ContextAwareBase implements Configurator
{
    public NoopConfigurator()
    {
        super();
    }

    @Override
    public ExecutionStatus configure(final LoggerContext loggerContext)
    {
        return null;
    }
}
