// Created: 09.02.2019
package de.freese.spring.cloud.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Thomas Freese
 */
@ConfigurationProperties
public class UriConfiguration {
    private String httpbin = "http://httpbin.org:80";

    public String getHttpbin() {
        return this.httpbin;
    }

    public void setHttpbin(final String httpbin) {
        this.httpbin = httpbin;
    }
}
