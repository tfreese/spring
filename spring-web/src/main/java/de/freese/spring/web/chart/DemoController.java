package de.freese.spring.web.chart;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.faces.view.ViewScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

/**
 * @author Thomas Freese
 */
// @Named
@Controller // see faces-config.xml: el-resolver
@ViewScoped
public final class DemoController implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoController.class);

    @Serial
    private static final long serialVersionUID = 3966368804680062710L;

    private final transient DataService dataService;

    public DemoController(final DataService dataService) {
        super();

        this.dataService = Objects.requireNonNull(dataService, "dataService required");
    }

    public List<Map.Entry<LocalDateTime, Double>> getData() {
        LOGGER.info("getData");

        return dataService.getData();
    }

    public String getJndiValue() throws NamingException {
        LOGGER.info("getJndiValue");

        return InitialContext.doLookup("java:comp/env/test");
    }

    public LocalDateTime getLocalDateTime() {
        LOGGER.info("getLocalDateTime");

        return dataService.getLocalDateTime();
    }
}
