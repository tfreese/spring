// Created: 25.03.2026
package de.spring.proxy;

import java.io.Serial;

import jakarta.servlet.http.HttpServletRequest;

import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.ee11.proxy.ProxyServlet;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * localhost:7070
 *
 * @author Thomas Freese
 */
public final class ProxyApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyApplication.class);

    static void main() {
        try {
            initProxy();
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private static void initProxy() throws Exception {
        final int listenPort = 7070;
        final int targetPort = 8080;

        final ProxyServlet proxyServlet = new ProxyServlet.Transparent() {
            @Serial
            private static final long serialVersionUID = 6120051423772765324L;

            @Override
            protected void addProxyHeaders(final HttpServletRequest clientRequest, final Request proxyRequest) {
                super.addProxyHeaders(clientRequest, proxyRequest);

                LOGGER.debug("addProxyHeaders: {}", proxyRequest.getURI());

                // Add Header for Target.
                proxyRequest.headers(headers -> headers.put("myHeader", "myValue"));
            }

            // @Override
            // protected Logger createLogger() {
            //     return LOGGER;
            // }
        };

        // The Servlet name is required to configure Logging.
        // See AbstractProxyServlet.createLogger.
        // See simplelogger.properties.
        final ServletHolder holder = new ServletHolder("proxyServlet", proxyServlet);

        // Forward to other local Port.
        // holder.setInitParameter("proxyTo", "http://localhost:" + targetPort);
        holder.setInitParameter("proxyTo", "https://www.google.com");

        // Keep Paths.
        holder.setInitParameter("prefix", "/");

        final Server server = new Server(listenPort);

        final ServletContextHandler servletContextHandler = new ServletContextHandler("/");
        servletContextHandler.addServlet(holder, "/*");

        server.setHandler(servletContextHandler);
        server.start();

        LOGGER.info("Reverse proxy started: http://localhost:{} -> http://localhost:{}", listenPort, targetPort);

        server.join();

    }

    private ProxyApplication() {
        super();
    }
}
