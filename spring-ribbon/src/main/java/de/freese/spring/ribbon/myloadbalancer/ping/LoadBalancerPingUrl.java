// Created: 21.03.2018
package de.freese.spring.ribbon.myloadbalancer.ping;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

/**
 * Ping auf eine feste URL des Servers.
 *
 * @author Thomas Freese
 */
public class LoadBalancerPingUrl implements LoadBalancerPing
{

    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancerPingUrl.class);
    /**
     *
     */
    private final HttpMessageConverter<String> messageConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
    /**
     *
     */
    private String expectedContent;
    /**
     *
     */
    private volatile ClientHttpRequestFactory httpRequestFactory;
    /**
     *
     */
    private boolean isSecure;
    /**
     *
     */
    private String pingAppendString = "";

    /**
     * Welcher Content muss der Ping liefern ?
     *
     * @return String
     */
    public String getExpectedContent()
    {
        return this.expectedContent;
    }

    /**
     * @return {@link ClientHttpRequestFactory}
     */
    public ClientHttpRequestFactory getHttpRequestFactory()
    {
        if (this.httpRequestFactory == null)
        {
            synchronized (this)
            {
                // DpubleCheckcLock
                if (this.httpRequestFactory == null)
                {
                    this.httpRequestFactory = new SimpleClientHttpRequestFactory();
                }
            }
        }

        return this.httpRequestFactory;
    }

    /**
     * Erweiterung der URL für den Ping.<br>
     * Beispiel: /service/ping
     *
     * @return String
     */
    public String getPingAppendString()
    {
        return this.pingAppendString;
    }

    /**
     * @see de.freese.spring.ribbon.myloadbalancer.ping.LoadBalancerPing#isAlive(java.lang.String)
     */
    @Override
    public boolean isAlive(final String server)
    {
        boolean isAlive = false;

        String urlStr = "";

        if (this.isSecure)
        {
            urlStr = "https://";
        }
        else
        {
            urlStr = "http://";
        }

        urlStr += server;
        urlStr += getPingAppendString();

        try
        {
            ClientHttpRequest request = getHttpRequestFactory().createRequest(new URL(urlStr).toURI(), HttpMethod.GET);

            String content = null;

            try (ClientHttpResponse response = request.execute())
            {
                isAlive = response.getRawStatusCode() == 200; // 200; HttpStatus.OK.value()

                content = this.messageConverter.read(String.class, response);
            }

            // HttpURLConnection connection = (HttpURLConnection) new URL(urlStr).openConnection();
            // connection.setRequestMethod("GET");
            // String content = getContent(connection);
            // isAlive = connection.getResponseCode() == 200; // 200; HttpStatus.OK.value()
            // connection.disconnect();
            if (getExpectedContent() != null)
            {
                if (content == null)
                {
                    isAlive = false;
                }
                else
                {
                    isAlive = checkAliveByContent(getExpectedContent(), content);
                }
            }

            return isAlive;
        }
        catch (Exception ex)
        {
            LOGGER.warn("{}: {}", urlStr, ex.getMessage());
        }

        return isAlive;
    }

    /**
     * true = https; false = http
     *
     * @return boolean
     */
    public boolean isSecure()
    {
        return this.isSecure;
    }

    /**
     * Welcher Content muss der Ping liefern ?
     *
     * @param expectedContent String; optional
     */
    public void setExpectedContent(final String expectedContent)
    {
        this.expectedContent = expectedContent;
    }

    /**
     * @param httpRequestFactory {@link ClientHttpRequestFactory}
     */
    public void setHttpRequestFactory(final ClientHttpRequestFactory httpRequestFactory)
    {
        this.httpRequestFactory = Objects.requireNonNull(httpRequestFactory, "httpRequestFactory required");
    }

    /**
     * Erweiterung der URL für den Ping.<br>
     * Beispiel: /service/ping
     *
     * @param pingAppendString String
     */
    public void setPingAppendString(final String pingAppendString)
    {
        this.pingAppendString = pingAppendString;
    }

    /**
     * true = https; false = http
     *
     * @param isSecure boolean
     */
    public void setSecure(final boolean isSecure)
    {
        this.isSecure = isSecure;
    }

    /**
     * Prüft den erwarteten Inhalt des isAlive-Requests.
     *
     * @param expectedContent String
     * @param returnedContent String
     *
     * @return boolean; true, wenn der Content den erwarteten Wert hat
     */
    protected boolean checkAliveByContent(final String expectedContent, final String returnedContent)
    {
        return returnedContent.equals(expectedContent);
    }

    /**
     * @param inputStream {@link InputStream}
     *
     * @return String
     *
     * @throws IOException Falls was schiefgeht.
     */
    protected String getContent(final InputStream inputStream) throws IOException
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

            return StandardCharsets.UTF_8.decode(byteBuffer).toString();
        }
    }
}
