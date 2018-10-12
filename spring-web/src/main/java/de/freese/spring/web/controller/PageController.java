/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose Tools | Templates and open the template in
 * the editor.
 */
package de.freese.spring.web.controller;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.annotation.RequestScope;

/**
 * @author Thomas Freese
 */
// @ManagedBean(name = "pageController")
// @RequestScoped
@Controller("pageController")
// @Scope("request")
@RequestScope
public class PageController implements Serializable
{
    /**
     *
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("PageController");

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Erstellt ein neues {@link PageController} Object.
     */
    public PageController()
    {
        super();

        LOGGER.info("create PageController");
    }

    /**
     *
     */
    @PostConstruct
    public void postConstruct()
    {
        LOGGER.info("postConstruct");
    }

    /**
     * Liefert outcome in Abhängigkeit von pageID, benötigt navigation-rule in faces.config.<br>
     * Oder den Namen der xhtml-Datei für Implizite Navigation liefern.
     *
     * @return String
     */
    public String processPage1()
    {
        return "success";
    }
}
