package de.jsf.controller;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;

import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
// @Named
@Component
@SessionScoped
public class LocaleController implements Serializable {
    private static final Locale WEB_DEFAULT_LOCALE = Locale.GERMANY;
    @Serial
    private static final long serialVersionUID = 2155318931496756025L;
    private Locale locale = WEB_DEFAULT_LOCALE;

    public Locale getLocale() {
        return locale;
    }

    public void updateLocale(final Locale locale) {
        this.locale = locale == null ? WEB_DEFAULT_LOCALE : locale;

        final FacesContext facesContext = FacesContext.getCurrentInstance();

        if (facesContext != null && facesContext.getViewRoot() != null) {
            facesContext.getViewRoot().setLocale(locale);
        }
    }
}
