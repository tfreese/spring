// Created: 01.03.2017
package de.freese.spring.hystrix.primarysecondary;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;
import com.netflix.hystrix.HystrixThreadPoolKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Freese
 */
public class PrimarySecondaryCommand extends HystrixCommand<String> {
    private static final Logger C_LOGGER = LoggerFactory.getLogger(PrimarySecondaryCommand.class);
    private static final DynamicBooleanProperty USE_PRIMARY = DynamicPropertyFactory.getInstance().getBooleanProperty("primarySecondary.usePrimary", true);

    /**
     * @author Thomas Freese
     */
    private static final class PrimaryCommand extends HystrixCommand<String> {
        private static final Logger P_LOGGER = LoggerFactory.getLogger(PrimaryCommand.class);

        private final int id;

        private PrimaryCommand(final int id) {
            // @formatter:off
            super(Setter
                    .withGroupKey(HystrixCommandGroupKey.Factory.asKey("test"))
                    .andCommandKey(HystrixCommandKey.Factory.asKey("PrimaryCommand"))
                    .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("PrimaryCommand"))
                    .andCommandPropertiesDefaults(
                            // we default to a 600ms timeout for primary
                            HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(600)));
            // @formatter:on

            this.id = id;
        }

        @Override
        protected String run() {
            P_LOGGER.info("run");

            // perform expensive 'primary' service call
            return "responseFromPrimary-" + this.id;
        }
    }

    /**
     * @author Thomas Freese
     */
    private static final class SecondaryCommand extends HystrixCommand<String> {
        private static final Logger S_LOGGER = LoggerFactory.getLogger(SecondaryCommand.class);

        private final int id;

        private SecondaryCommand(final int id) {
            // @formatter:off
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("test"))
                    .andCommandKey(HystrixCommandKey.Factory.asKey("SecondaryCommand"))
                    .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("SecondaryCommand"))
                    .andCommandPropertiesDefaults(
                            // we default to a 100ms timeout for secondary
                            HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(100)));
            // @formatter:on

            this.id = id;
        }

        @Override
        protected String run() {
            S_LOGGER.info("run");

            // perform fast 'secondary' service call
            return "responseFromSecondary-" + this.id;
        }
    }

    private final int id;

    public PrimarySecondaryCommand(final int id) {
        // @formatter:off
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("test"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("PrimarySecondaryCommand"))
                .andCommandPropertiesDefaults(
                // we want to default to semaphore-isolation since this wraps
                // 2 others commands that are already thread isolated
                HystrixCommandProperties.Setter().withExecutionIsolationStrategy(ExecutionIsolationStrategy.SEMAPHORE)));
        // @formatter:on

        this.id = id;
    }

    @Override
    protected String getCacheKey() {
        return String.valueOf(this.id);
    }

    @Override
    protected String run() throws Exception {
        C_LOGGER.info("run");

        if (USE_PRIMARY.get()) {
            return new PrimaryCommand(this.id).execute();
        }

        return new SecondaryCommand(this.id).execute();
    }
}
