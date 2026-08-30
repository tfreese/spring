package de.freese.spring.data.jpa.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author Thomas Freese
 * @since 16.08.2023
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum Status {
    PENDING,
    COMPLETED,
    CANCELED
}
