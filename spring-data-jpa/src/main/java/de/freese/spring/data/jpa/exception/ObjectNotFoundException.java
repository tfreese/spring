package de.freese.spring.data.jpa.exception;

import java.io.Serial;
import java.util.UUID;

public final class ObjectNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -2553758363006884166L;

    private final String className;
    private final UUID id;

    public ObjectNotFoundException(final Class<?> clazz, final UUID id) {
        this(clazz.getSimpleName(), id);
    }

    public ObjectNotFoundException(final String className, final UUID id) {
        super(className + " with ID " + id + " not found.");

        this.className = className;
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public UUID getId() {
        return id;
    }
}
