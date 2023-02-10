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
public class LoadBalancerPingUrl implements LoadBalancerPing {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancerPingUrl.class);

    private final HttpMessageConverter<String> messageConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);

    private String expectedContent;

    private volatile ClientHttpRequestFactory httpRequestFactory;

    private boolean isSecure;

    private String pingAppendString = "";

    /**
     * Welcher Content muss der Ping liefern ?
     */
    public String getExpectedContent() {
        return this.expectedContent;
    }

    public ClientHttpRequestFactory getHttpRequestFactory() {
        if (this.httpRequestFactory == null) {
            synchronized (this) {
                // DoubleCheckLock
                if (this.httpRequestFactory == null) {
                    this.httpRequestFactory = new SimpleClientHttpRequestFactory();
                }
            }
        }

        return this.httpRequestFactory;
    }

    /**
     * Erweiterung der URL für den Ping.<br>
     * Beispiel: /service/ping
     */
    public String getPingAppendString() {
        return this.pingAppendString;
    }

    /**
     * @see de.freese.spring.ribbon.myloadbalancer.ping.LoadBalancerPing#isAlive(java.lang.String)
     */
    @Override
    public boolean isAlive(final String server) {
        boolean isAlive = false;

        String urlStr = "";

        if (this.isSecure) {
            urlStr = "https://";
        }
        else {
            urlStr = "http://";
        }

        urlStr += server;
        urlStr += getPingAppendString();

        try {
            ClientHttpRequest request = getHttpRequestFactory().createRequest(new URL(urlStr).toURI(), HttpMethod.GET);

            String content = null;

            try (ClientHttpResponse response = request.execute()) {
                isAlive = response.getStatusCode().value() == 200; // 200; HttpStatus.OK.value()

                content = this.messageConverter.read(String.class, response);
            }

            // HttpURLConnection connection = (HttpURLConnection) new URL(urlStr).openConnection();
            // connection.setRequestMethod("GET");
            // String content = getContent(connection);
            // isAlive = connection.getResponseCode() == 200; // 200; HttpStatus.OK.value()
            // connection.disconnect();
            if (getExpectedContent() != null) {
                if (content == null) {
                    isAlive = false;
                }
                else {
                    isAlive = checkAliveByContent(getExpectedContent(), content);
                }
            }

            return isAlive;
        }
        catch (Exception ex) {
            LOGGER.warn("{}: {}", urlStr, ex.getMessage());
        }

        return isAlive;
    }

    /**
     * true = https; false = http
     */
    public boolean isSecure() {
        return this.isSecure;
    }

    /**
     * Welcher Content muss der Ping liefern ?
     *
     * @param expectedContent String; optional
     */
    public void setExpectedContent(final String expectedContent) {
        this.expectedContent = expectedContent;
    }

    public void setHttpRequestFactory(final ClientHttpRequestFactory httpRequestFactory) {
        this.httpRequestFactory = Objects.requireNonNull(httpRequestFactory, "httpRequestFactory required");
    }

    /**
     * Erweiterung der URL für den Ping.<br>
     * Beispiel: /service/ping
     */
    public void setPingAppendString(final String pingAppendString) {
        this.pingAppendString = pingAppendString;
    }

    /**
     * true = https; false = http
     */
    public void setSecure(final boolean isSecure) {
        this.isSecure = isSecure;
    }

    /**
     * Prüft den erwarteten Inhalt des isAlive-Requests.
     *
     * @return boolean; true, wenn der Content den erwarteten Wert hat
     */
    protected boolean checkAliveByContent(final String expectedContent, final String returnedContent) {
        return returnedContent.equals(expectedContent);
    }

    protected String getContent(final InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }

        try (ReadableByteChannel channel = Channels.newChannel(inputStream)) {
            int capacity = inputStream.available();

            ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);

            channel.read(byteBuffer);
            byteBuffer.rewind();

            return StandardCharsets.UTF_8.decode(byteBuffer).toString();
        }
    }
}
