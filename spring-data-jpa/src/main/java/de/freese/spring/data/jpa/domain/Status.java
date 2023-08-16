// Created: 16.08.23
package de.freese.spring.data.jpa.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author Thomas Freese
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum Status {
    PENDING,
    COMPLETED,
    CANCELED
}
