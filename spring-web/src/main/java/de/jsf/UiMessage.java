package de.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/**
 * @author Thomas Freese
 */
public final class UiMessage {

    public static void addMessage(final FacesMessage.Severity severity, final String ressourceTextKey) {
        final FacesContext context = FacesContext.getCurrentInstance();
        final String text = context.getApplication().evaluateExpressionGet(context, String.format("#{text['%s']}", ressourceTextKey), String.class);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, text, ""));
    }

    public static void addMessage(final FacesMessage.Severity severity, final String ressourceTextKey, final String detailText) {
        final FacesContext context = FacesContext.getCurrentInstance();
        final String text = context.getApplication().evaluateExpressionGet(context, String.format("#{text['%s']}", ressourceTextKey), String.class);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, text, detailText));
    }

    private UiMessage() {
        super();
    }
}
