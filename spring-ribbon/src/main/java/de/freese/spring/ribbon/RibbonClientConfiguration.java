// Created: 05.03.2018
package de.freese.spring.ribbon;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AvailabilityFilteringRule;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.PingUrl;
import com.netflix.loadbalancer.Server;

/**
 * an IClientConfig, which stores client configuration for a client or load balancer,<br>
 * an ILoadBalancer, which represents a software load balancer,<br>
 * a ServerList, which defines how to get a list of servers to choose from,<br>
 * an IRule, which describes a load balancing strategy, and<br>
 * an IPing, which says how periodic pings of a server are performed.<br>
 *
 * @author Thomas Freese
 * @see LoadBalancerAutoConfiguration
 */
public class RibbonClientConfiguration
{
    /**
     * Führt den gleichen Code wie {@link PingUrl} aus, aber führt nicht den printStackTrace aus bei einem Fehler.
     *
     * @author Thomas Freese
     */
    private class MyPing extends PingUrl
    {
        /**
         *
         */
        private final Logger logger = LoggerFactory.getLogger(MyPing.class);

        /**
         * Erzeugt eine neue Instanz von {@link MyPing}.
         */
        public MyPing()
        {
            super();
        }

        /**
         * @param connection {@link HttpURLConnection}
         * @return String
         */
        private String getContent(final HttpURLConnection connection)
        {
            try (InputStream inputStream = connection.getInputStream())
            {
                if (inputStream == null)
                {
                    return null;
                }

                try (ReadableByteChannel channel = Channels.newChannel(inputStream))
                {
                    int capacity = inputStream.available();

                    ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);

                    channel.read(byteBuffer);
                    byteBuffer.rewind();

                    Charset charset = StandardCharsets.UTF_8;
                    String content = charset.decode(byteBuffer).toString();

                    return content;
                }
            }
            catch (IOException iex)
            {
                // Ignore
            }

            return null;
        }

        /**
         * @see com.netflix.loadbalancer.PingUrl#isAlive(com.netflix.loadbalancer.Server)
         */
        @Override
        public boolean isAlive(final Server server)
        {
            this.logger.debug("pinging: " + server);

            String urlStr = "";

            if (isSecure())
            {
                urlStr = "https://";
            }
            else
            {
                urlStr = "http://";
            }

            urlStr += server.getId();
            urlStr += getPingAppendString();

            boolean isAlive = false;

            // HttpClient httpClient = new DefaultHttpClient();
            // HttpUriRequest getRequest = new HttpGet(urlStr);
            try
            {
                // HttpResponse response = httpClient.execute(getRequest);
                // String content = EntityUtils.toString(response.getEntity());
                // isAlive = response.getStatusLine().getStatusCode() == 200;
                HttpURLConnection connection = (HttpURLConnection) new URL(urlStr).openConnection();
                connection.setRequestMethod("GET");
                String content = getContent(connection);
                isAlive = connection.getResponseCode() == 200;
                connection.disconnect();

                if (getExpectedContent() != null)
                {
                    if (content == null)
                    {
                        isAlive = false;
                    }
                    else
                    {
                        if (content.equals(getExpectedContent()))
                        {
                            isAlive = true;
                        }
                        else
                        {
                            isAlive = false;
                        }
                    }
                }
            }
            catch (IOException ex)
            {
                // ex.printStackTrace();
                this.logger.warn(server + ": " + ex.getMessage());
            }
            // finally
            // {
            // // Release the connection.
            // getRequest.abort();
            // }

            return isAlive;
        }
    }

    /**
     *
     */
    @Resource
    private IClientConfig ribbonClientConfig = null;

    /**
     * Erzeugt eine neue Instanz von {@link RibbonClientConfiguration}.
     */
    public RibbonClientConfiguration()
    {
        super();
    }

    /**
     * @param config {@link IClientConfig}
     * @return {@link IPing}
     */
    @Bean
    public IPing ribbonPing(final IClientConfig config)
    {
        PingUrl ping = new MyPing();
        ping.setPingAppendString("/service/ping"); // /netflix/service/actuator/health
        ping.setExpectedContent("true"); // UP

        // IPing ping = new NoOpPing();
        return ping;
    }

    /**
     * @param config {@link IClientConfig}
     * @return {@link IRule}
     */
    @Bean
    public IRule ribbonRule(final IClientConfig config)
    {
        return new AvailabilityFilteringRule();
    }
}
