package de.jsf.controller;

import java.io.IOException;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;

import de.jsf.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
// @Named
@Component
@ViewScoped
public class IndexController {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    // @Value("${server.servlet.context-path}")
    private final String contextPath;

    private String param1;
    private String redirectAppUrl;
    private User user;

    public IndexController(@Value("${server.servlet.context-path}") final String contextPath) {
        super();

        this.contextPath = contextPath;
    }

    public String getParam1() {
        return param1;
    }

    public String getRedirectAppUrl() {
        return redirectAppUrl;
    }

    public User getUser() {
        return user;
    }

    public void initParams() {
        LOGGER.info("Init params");

        this.user = new User();

        LOGGER.info("param1={}", this.param1);
    }

    public boolean isUserWebAdmin() {
        return false;
    }

    public void logout() throws IOException {
        LOGGER.info("Logout called");

        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

        // String contextRoot = RSConfiguration.getContextRoot();
        //
        // if (!contextRoot.endsWith("/")) {
        //     contextRoot += "/";
        // }

        FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath + "/content/index.jsf?param1=logged-out");
    }

    public void setParam1(final String param1) {
        this.param1 = param1;
    }
}
