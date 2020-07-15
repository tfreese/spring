// Created: 01.03.2017
package de.freese.spring.hystrix;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.client.RestTemplate;
import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;

/**
 * @author Thomas Freese
 */
public class LoadBalancerApplication
{
    /**
     * @author Thomas Freese
     */
    public static class HttpResponseHystrixCommand extends HystrixCommand<ClientHttpResponse>
    {
        /**
         *
         */
        private final byte[] body;

        /**
         *
         */
        private final ClientHttpRequestExecution execution;

        /**
         *
         */
        private final HttpRequest request;

        /**
         *
         */
        private final List<URI> uris;

        /**
         * Erzeugt eine neue Instanz von {@link HttpResponseHystrixCommand}.
         *
         * @param uris {@link List}
         * @param request {@link HttpRequest}
         * @param body byte[]
         * @param execution {@link ClientHttpRequestExecution}
         */
        public HttpResponseHystrixCommand(final List<URI> uris, final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution)
        {
            // CommandGroupKey = ThreadPool-Name
            // super(HystrixCommandGroupKey.Factory.asKey("sysDate" + level));

            // @formatter:off
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("hystrix"))
                    .andCommandKey(HystrixCommandKey.Factory.asKey("test-sysdate"))
                    .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("test-sysdate"))
                    .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(500)));
            // @formatter:on

            this.uris = Objects.requireNonNull(uris, "uris required");
            this.request = Objects.requireNonNull(request, "request required");
            this.body = Objects.requireNonNull(body, "body required");
            this.execution = Objects.requireNonNull(execution, "execution required");

            // System.out.println(getCommandGroup());
            // System.out.println(getCommandKey());
            // System.out.println(getThreadPoolKey());
        }

        /**
         * @see com.netflix.hystrix.HystrixCommand#getFallback()
         */
        @Override
        protected ClientHttpResponse getFallback()
        {
            if (this.uris.isEmpty())
            {
                // Keine weiteren URLs mehr vorhanden.
                return null;
            }

            HttpResponseHystrixCommand cmd = new HttpResponseHystrixCommand(this.uris, this.request, this.body, this.execution);

            return cmd.execute();
        }

        /**
         * @see com.netflix.hystrix.HystrixCommand#run()
         */
        @Override
        protected ClientHttpResponse run() throws Exception
        {
            // HystrixBadRequestException
            final URI uri = this.uris.remove(0);

            // System.out.println(repository.getHost());
            // System.out.println(repository.getPath());
            // System.out.println(repository.getFragment());

            HttpRequestWrapper requestWrapper = new HttpRequestWrapper(this.request)
            {
                /**
                 * @see org.springframework.http.client.support.HttpRequestWrapper#getURI()
                 */
                @Override
                public URI getURI()
                {
                    return uri;
                }
            };

            return this.execution.execute(requestWrapper, this.body);
        }
    }

    /**
     * Tauscht die URL aus und wiederholt den Request bis dieser erfolgreich ist.
     *
     * @author Thomas Freese
     */
    public static class LoadBalancerHystrixInterceptor implements ClientHttpRequestInterceptor
    {
        /**
         *
         */
        private final String[] server;

        /**
         *
         */
        private int serverIndex = 0;

        /**
         * Erzeugt eine neue Instanz von {@link LoadBalancerHystrixInterceptor}.
         *
         * @param server String[]
         */
        public LoadBalancerHystrixInterceptor(final String...server)
        {
            super();

            this.server = Objects.requireNonNull(server, "server required");
        }

        /**
         * Wandelt die Template-URI in die reale URI um.<br>
         * Beispiel: http://date-service/hystrix/test/sysdate -> http://localhost:65501/hystrix/test/sysdate
         *
         * @param originalUri {@link URI}
         * @param server String
         * @return {@link URI}
         * @throws IOException Falls was schief geht.
         */
        private URI convertURI(final URI originalUri, final String server) throws IOException
        {
            String url = originalUri.toString();
            url = url.replace("date-service", server);

            try
            {
                return new URI(url);
            }
            catch (URISyntaxException ex)
            {
                throw new IOException(ex);
            }
        }

        /**
         * @see org.springframework.http.client.ClientHttpRequestInterceptor#intercept(org.springframework.http.HttpRequest, byte[],
         *      org.springframework.http.client.ClientHttpRequestExecution)
         */
        @Override
        public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException
        {
            final URI originalUri = request.getURI();

            List<URI> uris = new ArrayList<>();
            int count = this.server.length;

            for (int i = 0; i < count; i++)
            {
                uris.add(convertURI(originalUri, nextServer()));
            }

            nextServer();

            HttpResponseHystrixCommand command = new HttpResponseHystrixCommand(uris, request, body, execution);

            return command.execute();
        }

        /**
         * Liefert den nächsten Server.
         *
         * @return String
         */
        private String nextServer()
        {
            String host = this.server[this.serverIndex++];

            if (this.serverIndex == this.server.length)
            {
                this.serverIndex = 0;
            }

            return host;
        }
    }

    /**
     * Tauscht die URL aus und wiederholt den Request bis dieser erfolgreich ist.
     *
     * @author Thomas Freese
     */
    public static class LoadBalancerInterceptor implements ClientHttpRequestInterceptor
    {
        /**
         *
         */
        private int index = 0;

        /**
         *
         */
        private final String[] server;

        /**
         * Erzeugt eine neue Instanz von {@link LoadBalancerInterceptor}.
         *
         * @param server String[]
         */
        public LoadBalancerInterceptor(final String...server)
        {
            super();

            this.server = Objects.requireNonNull(server, "server required");
        }

        /**
         * Wandelt die Template-URI in die reale URI um.<br>
         * Beispiel: http://date-service/hystrix/test/sysdate -> http://localhost:65501/hystrix/test/sysdate
         *
         * @param originalUri {@link URI}
         * @param server String
         * @return {@link URI}
         * @throws IOException Falls was schief geht.
         */
        private URI convertURI(final URI originalUri, final String server) throws IOException
        {
            String url = originalUri.toString();
            url = url.replace("date-service", server);

            try
            {
                return new URI(url);
            }
            catch (URISyntaxException ex)
            {
                throw new IOException(ex);
            }
        }

        /**
         * @see org.springframework.http.client.ClientHttpRequestInterceptor#intercept(org.springframework.http.HttpRequest, byte[],
         *      org.springframework.http.client.ClientHttpRequestExecution)
         */
        @Override
        public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException
        {
            final URI originalUri = request.getURI();
            int trys = this.server.length;

            IOException lastException = null;

            for (int i = 0; i < trys; i++)
            {
                final String server = nextServer();
                final URI newUri = convertURI(originalUri, server);

                try
                {
                    ClientHttpResponse response = intercept(newUri, request, body, execution);

                    return response;
                }
                catch (IOException ex)
                {
                    lastException = ex;
                }
            }

            if (lastException != null)
            {
                throw lastException;
            }

            return null;
        }

        /**
         * @param newUri {@link URI}
         * @param request {@link HttpRequest}
         * @param body byte[]
         * @param execution {@link ClientHttpRequestExecution}
         * @return {@link ClientHttpResponse}
         * @throws IOException Falls was schief geht.
         */
        private ClientHttpResponse intercept(final URI newUri, final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution)
            throws IOException
        {
            HttpRequestWrapper requestWrapper = new HttpRequestWrapper(request)
            {
                /**
                 * @see org.springframework.http.client.support.HttpRequestWrapper#getURI()
                 */
                @Override
                public URI getURI()
                {
                    return newUri;
                }
            };

            return execution.execute(requestWrapper, body);
        }

        /**
         * Liefert den nächsten Server.
         *
         * @return String
         */
        private String nextServer()
        {
            String host = this.server[this.index++];

            if (this.index == this.server.length)
            {
                this.index = 0;
            }

            return host;
        }
    }

    /**
     * @param args String[]
     * @throws Exception Falls was schief geht.
     */
    public static void main(final String[] args) throws Exception
    {
        // configuration from environment properties
        ConcurrentMapConfiguration configFromEnvironmentProperties = new ConcurrentMapConfiguration(new EnvironmentConfiguration());

        // configuration from system properties
        ConcurrentMapConfiguration configFromSystemProperties = new ConcurrentMapConfiguration(new SystemConfiguration());

        // // configuration from local properties file
        ConcurrentMapConfiguration configFromPropertiesFile = new ConcurrentMapConfiguration(new PropertiesConfiguration("hystrix.properties"));

        // create a hierarchy of configuration that makes
        // 1) system properties override properties file
        ConcurrentCompositeConfiguration finalConfig = new ConcurrentCompositeConfiguration();
        finalConfig.addConfiguration(configFromEnvironmentProperties, "environmentConfig");
        finalConfig.addConfiguration(configFromSystemProperties, "systemConfig");
        finalConfig.addConfiguration(configFromPropertiesFile, "fileConfig");

        // install with ConfigurationManager so that finalConfig becomes the source of dynamic properties
        ConfigurationManager.install(finalConfig);

        // RestTemplate restTemplate = new RestTemplateBuilder()
        // .additionalInterceptors(new LoadBalancerInterceptor("localhost:65501", "localhost:65502", "localhost:65503")).build();
        RestTemplate restTemplate = new RestTemplateBuilder()
                .additionalInterceptors(new LoadBalancerHystrixInterceptor("localhost:8081", "localhost:8082", "localhost:8083")).build();

        String url = "http://date-service/service/sysdate";

        while (true)
        {
            String result = restTemplate.getForObject(url, String.class);

            System.out.println(result);

            if (result == null)
            {
                break;
            }

            Thread.sleep(1000);
        }

        System.exit(0);
    }
}
