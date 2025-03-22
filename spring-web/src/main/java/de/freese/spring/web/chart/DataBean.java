package de.freese.spring.web.chart;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.annotation.Resource;
import jakarta.faces.view.ViewScoped;

import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component  // siehe faces-config.xml: el-resolver
@ViewScoped
public final class DataBean implements Serializable {
    @Serial
    private static final long serialVersionUID = 3966368804680062710L;

    @Resource
    private transient DataService dataService;

    public Map<LocalDateTime, Double> getData() {
        return dataService.getData();
    }

    public String getJndiValue() throws NamingException {
        return InitialContext.doLookup("java:comp/env/test");
    }

    public LocalDateTime getLocalDateTime() {
        return dataService.getLocalDateTime();
    }
}
