// Created: 29.01.24
package de.freese.spring.data.jpa.exception;

import java.io.Serial;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Thomas Freese
 */
public class ObjectNotFoundException extends ApplicationException {
    @Serial
    private static final long serialVersionUID = 2517834129407627398L;
    
    private final String entityName;
    private final UUID id;

    public ObjectNotFoundException(final Class<?> entity, final UUID id) {
        super();

        this.entityName = Objects.requireNonNull(entity, "entity required").getSimpleName();
        this.id = Objects.requireNonNull(id, "id required");
    }

    public String getEntityName() {
        return entityName;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String getMessage() {
        return "%s not found by ID: %s".formatted(getEntityName(), getId());
    }
}
