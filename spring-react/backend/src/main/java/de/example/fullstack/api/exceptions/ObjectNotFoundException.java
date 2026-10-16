package de.example.fullstack.api.exceptions;

import java.io.Serial;

public final class ObjectNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -2553758363006884166L;

    private final String className;
    private final long id;

    public ObjectNotFoundException(final Class<?> clazz, final long id) {
        this(clazz.getSimpleName(), id);
    }

    public ObjectNotFoundException(final String className, final long id) {
        super(className + " mit ID " + id + " nicht gefunden.");

        this.className = className;
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public long getId() {
        return id;
    }
}
