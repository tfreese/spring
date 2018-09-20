// Created: 01.03.2017
package de.freese.j2ee.spring.hystrix.sysdate;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;

/**
 * {@link HystrixCommand} mit Fallback über drei Server.<br>
 * <br>
 * https://github.com/Netflix/Hystrix/wiki/How-To-Use#Common-Patterns-FallbackCacheViaNetwork<br>
 *
 * @author Thomas Freese
 */
public class SysDateHystrixCommand extends HystrixCommand<String>
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SysDateHystrixCommand.class);

    /**
     *
     */
    private final int level;

    /**
     *
     */
    private RestTemplate restTemplate = null;

    /**
     *
     */
    private String[] urls = null;

    /**
     * Erzeugt eine neue Instanz von {@link SysDateHystrixCommand}
     */
    public SysDateHystrixCommand()
    {
        this(1);
    }

    /**
     * Erzeugt eine neue Instanz von {@link SysDateHystrixCommand}
     *
     * @param level int 1, 2, 3 ...
     */
    private SysDateHystrixCommand(final int level)
    {
        // CommandGroupKey = ThreadPool-Name
        // super(HystrixCommandGroupKey.Factory.asKey("sysDate" + level));

        // @formatter:off
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("test"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("sysDate"))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("sysDate" + level))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(300)));
        // @formatter:on

        this.level = level;
    }

    /**
     * @param restTemplate {@link RestTemplate}
     */
    public void setRestTemplate(final RestTemplate restTemplate)
    {
        this.restTemplate = restTemplate;
    }

    /**
     * @param urls String[]
     */
    public void setURLs(final String... urls)
    {
        this.urls = urls;
    }

    // /**
    // * Wird für Request Caching benötigt.
    // *
    // * @see com.netflix.hystrix.AbstractCommand#getCacheKey()
    // */
    // @Override
    // protected String getCacheKey()
    // {
    // return String.valueOf(myRequestValue);
    // }

    /**
     * @see com.netflix.hystrix.HystrixCommand#getFallback()
     */
    @Override
    protected String getFallback()
    {
        if (this.urls.length == 1)
        {
            // Keine weiteren URLs mehr vorhanden.
            return null;
        }

        LOGGER.info("");

        String[] fallbackURLs = (String[]) ArrayUtils.remove(this.urls, 0);

        SysDateHystrixCommand cmd = new SysDateHystrixCommand((this.level + 1));
        cmd.setRestTemplate(this.restTemplate);
        cmd.setURLs(fallbackURLs);

        return cmd.execute();
    }

    /**
     * @see com.netflix.hystrix.HystrixCommand#run()
     */
    @Override
    protected String run() throws Exception
    {
        String result = this.restTemplate.getForObject(this.urls[0], String.class);

        LOGGER.info("level={}: {}", this.level, result);

        return result;
    }
}
