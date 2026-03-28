// // Created: 25.03.2026
// package dep.spring.proxy;
//
// import java.io.Serial;
//
// import jakarta.servlet.http.HttpServletRequest;
//
// import org.eclipse.jetty.client.api.Request;
// import org.eclipse.jetty.proxy.ProxyServlet;
// import org.eclipse.jetty.server.Server;
// import org.eclipse.jetty.servlet.ServletContextHandler;
// import org.eclipse.jetty.servlet.ServletHolder;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
//
// /**
//  * @author Thomas Freese
//  */
// public final class ProxyApplication11026 {
//     private static final Logger LOGGER = LoggerFactory.getLogger(ProxyApplication11026.class);
//
//     static void main() {
//         try {
//             initProxy();
//         }
//         catch (Exception ex) {
//             LOGGER.error(ex.getMessage(), ex);
//         }
//     }
//
//     private static void initProxy() throws Exception {
//         final int listenPort = 7070;
//         final int targetPort = 8080;
//
//         final ProxyServlet proxy = new ProxyServlet.Transparent() {
//             @Serial
//             private static final long serialVersionUID = 6120051423772765324L;
//
//             @Override
//             protected void addProxyHeaders(final HttpServletRequest clientRequest, final Request proxyRequest) {
//                 super.addProxyHeaders(clientRequest, proxyRequest);
//
//                 // Add Header for Target.
//                 proxyRequest.headers(headers -> headers.put("myHeader", "myValue"));
//             }
//         };
//
//         final ServletHolder holder = new ServletHolder(proxy);
//
//         // Forward to other local Port.
//         // holder.setInitParameter("proxyTo", "http://localhost:" + targetPort);
//         holder.setInitParameter("proxyTo", "https://www.google.com");
//
//         // Keep Paths.
//         holder.setInitParameter("prefix", "/");
//
//         final Server server = new Server(listenPort);
//
//         // final ServletContextHandler servletContextHandler = new ServletContextHandler("/");
//         final ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/");
//         servletContextHandler.addServlet(holder, "/*");
//
//         // server.setHandler(servletContextHandler);
//         server.start();
//
//         LOGGER.info("Reverse proxy started: http://localhost:{} -> http://localhost:{}", listenPort, targetPort);
//
//         server.join();
//
//     }
//
//     private ProxyApplication11026() {
//         super();
//     }
// }
