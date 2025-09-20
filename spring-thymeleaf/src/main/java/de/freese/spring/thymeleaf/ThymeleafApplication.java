package de.freese.spring.thymeleaf;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.PropertyResolver;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * curl http://localhost:8080/rest/person/personList -X GET -H "Accept: application/xml" -v -u 'user:pw'<br>
 * curl --noproxy localhost -u 'user:pw' -v http://localhost:8080/api/auth<br>
 * curl http://localhost:8080/rest/person/personAdd -X POST -H "Content-Type: application/json" -v -u 'user:pw' -d '{"firstName":"Thomas","lastName":"Freese"}'
 * curl -i -X POST -d username=user -d password=pw -c /opt/cookies.txt<br>
 * curl -i --header "Accept:application/json" -X GET -b /opt/cookies.txt<br>
 * {"firstName":"Thomas","lastName":"Freese"}<br>
 * <br>
 * POM:<br>
 * &lt;packaging>&gt;war&lt;/packaging&gt;<<br>
 * Tomcat aus spring-boot-starter-web excludieren und explizit auf provided setzen.<br>
 * Alle anderen J2EE-Jars auf provided setzen.
 *
 * @author Thomas Freese
 */
@SpringBootApplication
@ComponentScan(basePackages = {"de.freese.spring.thymeleaf"})
@EnableScheduling
@EnableAsync
public class ThymeleafApplication extends SpringBootServletInitializer {

    public static HttpServletRequest getCurrentRequest() {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        Assert.state(requestAttributes != null, "Could not find current request via RequestContextHolder");
        Assert.isInstanceOf(ServletRequestAttributes.class, requestAttributes);

        final HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        Assert.state(servletRequest != null, "Could not find current HttpServletRequest");

        return servletRequest;
    }

    public static String getRootUri(final PropertyResolver propertyResolver) {
        final int port = Optional.ofNullable(propertyResolver.getProperty("local.server.port", Integer.class))
                .or(() -> Optional.ofNullable(propertyResolver.getProperty("server.port", Integer.class)))
                .orElse(0);
        final Optional<String> contextPath = Optional.ofNullable(propertyResolver.getProperty("server.servlet.context-path", String.class));
        final Optional<Boolean> sslEnabled = Optional.ofNullable(propertyResolver.getProperty("server.ssl.enabled", Boolean.class));

        final String protocol = sslEnabled.orElse(false) ? "https" : "http";

        return protocol + "://localhost:" + port + contextPath.orElse("");
    }

    static void main(final String[] args) {
        // ApplicationContext context = SpringApplication.run(SpringBootThymeleafApplication.class, args);
        configureApplication(new SpringApplicationBuilder()).run(args);
    }

    private static SpringApplicationBuilder configureApplication(final SpringApplicationBuilder builder) {
        // headless(false) für Desktop
        // .bannerMode(Banner.Mode.OFF);
        // .profiles(profiles)
        return builder.sources(ThymeleafApplication.class).headless(true); // .profiles("with-ssl");
    }

    /**
     * POM:<br>
     * &lt;packaging>&gt;war&lt;/packaging&gt;<<br>
     * Tomcat aus spring-boot-starter-web excludieren und explizit auf provided setzen.<br>
     * Alle anderen J2EE-Jars auf provided setzen.
     */
    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
        return configureApplication(application);
    }
}
