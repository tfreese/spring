// Created: 01.03.2017
package de.freese.spring.hystrix.sysdate;

import java.util.List;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

/**
 * {@link HystrixCommand} mit Fallback über drei Server.<br>
 * <br>
 * <a href="https://github.com/Netflix/Hystrix/wiki/How-To-Use#Common-Patterns-FallbackCacheViaNetwork">Hystrix</a><br>
 *
 * @author Thomas Freese
 */
public class SysDateHystrixCommand extends HystrixCommand<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SysDateHystrixCommand.class);

    private final int level;

    private RestTemplate restTemplate;
    private List<String> urls;

    public SysDateHystrixCommand() {
        this(1);
    }

    /**
     * @param level int 1, 2, 3 ...
     */
    private SysDateHystrixCommand(final int level) {
        // CommandGroupKey = ThreadPool-Name
        // super(HystrixCommandGroupKey.Factory.asKey("sysDate" + level));

        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("test"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("sysDate"))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("sysDate" + level))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(300)));

        this.level = level;
    }

    public void setRestTemplate(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setURLs(final List<String> urls) {
        this.urls = List.copyOf(urls);
    }

    // /**
    // * Wird für Request Caching benötigt.
    // */
    // @Override
    // protected String getCacheKey() {
    // return String.valueOf(myRequestValue);
    // }

    @Override
    protected String getFallback() {
        if (urls.size() == 1) {
            // Keine weiteren URLs mehr vorhanden.
            return null;
        }

        LOGGER.info("");

        final List<String> fallbackURLs = urls.subList(1, urls.size());

        final SysDateHystrixCommand cmd = new SysDateHystrixCommand(level + 1);
        cmd.setRestTemplate(restTemplate);
        cmd.setURLs(fallbackURLs);

        return cmd.execute();
    }

    @Override
    protected String run() {
        final String result = restTemplate.getForObject(urls.getFirst(), String.class);

        LOGGER.info("level={}: {}", level, result);

        return result;
    }
}
