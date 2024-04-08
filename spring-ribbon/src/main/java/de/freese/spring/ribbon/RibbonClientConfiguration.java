// Created: 05.03.2018
package de.freese.spring.ribbon;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

import jakarta.annotation.Resource;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AvailabilityFilteringRule;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.PingUrl;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

/**
 * an IClientConfig, which stores client configuration for a client or load balancer,<br>
 * an ILoadBalancer, which represents a software load balancer,<br>
 * a ServerList, which defines how to get a list of servers to choose from,<br>
 * an IRule, which describes a load balancing strategy, and<br>
 * an IPing, which says how periodic pings of a server are performed.<br>
 *
 * @author Thomas Freese
 */
public class RibbonClientConfiguration {
    /**
     * Führt den gleichen Code wie {@link PingUrl} aus, aber führt nicht den printStackTrace aus bei einem Fehler.
     *
     * @author Thomas Freese
     */
    private static final class MyPing extends PingUrl {
        private static String getContent(final HttpURLConnection connection) {
            try (InputStream inputStream = connection.getInputStream()) {
                if (inputStream == null) {
                    return null;
                }

                try (ReadableByteChannel channel = Channels.newChannel(inputStream)) {
                    final int capacity = inputStream.available();

                    final ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);

                    channel.read(byteBuffer);
                    byteBuffer.rewind();

                    return StandardCharsets.UTF_8.decode(byteBuffer).toString();
                }
            }
            catch (IOException iex) {
                // Empty
            }

            return null;
        }

        private final Logger logger = LoggerFactory.getLogger(MyPing.class);

        @Override
        public boolean isAlive(final Server server) {
            this.logger.debug("pinging: {}", server);

            String uriStr = "";

            if (isSecure()) {
                uriStr = "https://";
            }
            else {
                uriStr = "http://";
            }

            uriStr += server.getId();
            uriStr += getPingAppendString();

            boolean isAlive = false;

            // HttpClient httpClient = new DefaultHttpClient();
            // HttpUriRequest getRequest = new HttpGet(urlStr);
            try {
                // HttpResponse response = httpClient.execute(getRequest);
                // String content = EntityUtils.toString(response.getEntity());
                // isAlive = response.getStatusLine().getStatusCode() == 200;
                final HttpURLConnection connection = (HttpURLConnection) URI.create(uriStr).toURL().openConnection();
                connection.setRequestMethod("GET");
                final String content = getContent(connection);
                isAlive = connection.getResponseCode() == 200;
                connection.disconnect();

                if (getExpectedContent() != null) {
                    if (content == null) {
                        isAlive = false;
                    }
                    else {
                        isAlive = content.equals(getExpectedContent());
                    }
                }
            }
            catch (IOException ex) {
                // ex.printStackTrace();
                this.logger.warn("{}: {}", server, ex.getMessage());
            }
            // finally {
            // // Release the connection.
            // getRequest.abort();
            // }

            return isAlive;
        }
    }

    @Resource
    private IClientConfig ribbonClientConfig;

    @Bean
    public IPing ribbonPing(final IClientConfig config) {
        final PingUrl ping = new MyPing();
        ping.setPingAppendString("/service/ping"); // /netflix/service/actuator/health
        ping.setExpectedContent("true"); // UP

        // IPing ping = new NoOpPing();
        return ping;
    }

    @Bean
    public IRule ribbonRule(final IClientConfig config) {
        return new AvailabilityFilteringRule();
    }
}
