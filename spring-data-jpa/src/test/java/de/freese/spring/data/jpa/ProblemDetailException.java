package de.freese.spring.data.jpa;

import java.io.Serial;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ProblemDetail;

/**
 * @author Thomas Freese
 * @since 26.11.26
 */
public final class ProblemDetailException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -5603667404907232227L;

    public static ProblemDetailException of(final ProblemDetail problemDetail) {
        final String title = Optional.ofNullable(problemDetail.getTitle()).orElse("Unknown Message");
        final String detail = Optional.ofNullable(problemDetail.getDetail()).orElse("");
        final int status = problemDetail.getStatus();
        final String message = detail.isBlank() ? title : title + ": " + detail;

        return new ProblemDetailException(message, title, detail, status, problemDetail.getProperties());
    }

    private final String detail;
    private final transient Map<String, Object> properties;
    private final int status;
    private final String title;

    private ProblemDetailException(final String message,
                                   final String title,
                                   final String detail,
                                   final int status,
                                   final Map<String, Object> properties) {
        super(message);

        this.title = title;
        this.detail = detail;
        this.status = status;
        this.properties = Optional.ofNullable(properties).orElse(Map.of());
    }

    public String getDetail() {
        return detail;
    }

    public Map<String, Object> getProperties() {
        return Map.copyOf(properties);
    }

    public Object getProperty(final String key) {
        return properties.get(key);
    }

    public int getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }
}