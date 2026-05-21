package de.jsf;

import java.io.Serial;
import java.io.Serializable;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;

import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
// @Named
@Component
@RequestScoped
public final class UiConfig implements Serializable {
    // private static final String DEFAULT_THEME = "otherTheme";
    private static final String DEFAULT_THEME = "default";
    
    @Serial
    private static final long serialVersionUID = -7320013833103016092L;

    public static long asMb(final Number kB) {
        if (kB == null) {
            return 0L;
        }

        return (long) (kB.doubleValue() / 1_024D);
    }

    public String getCssPath(final String cssFile) {
        return getThemePath() + "/css/" + cssFile;
    }

    public String getCssResourcesPath(final String cssFile) {
        return getThemePath() + ":css/" + cssFile;
    }

    public String getFontResourcesPath(final String fontFile) {
        return getThemePath() + ":font/" + fontFile;
    }

    public String getImgPath(final String imgFile) {
        return getThemePath() + "/img/" + imgFile;
    }

    public String getImgResourcesPath(final String imgFile) {
        return getThemePath() + ":img/" + imgFile;
    }

    public void userIdleSession() {
        UiMessage.addMessage(FacesMessage.SEVERITY_WARN, "login.sessionAbgelaufen");
    }

    private String getThemePath() {
        return DEFAULT_THEME;
    }
}
