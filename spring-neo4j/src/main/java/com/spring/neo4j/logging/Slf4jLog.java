// Created: 30.06.2025
package com.spring.neo4j.logging;

import java.util.Objects;

import org.neo4j.logging.Log;
import org.slf4j.Logger;

/**
 * @author Thomas Freese
 */
public record Slf4jLog(Logger logger) implements Log {
    public Slf4jLog(final Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger required");
    }

    @Override
    public void debug(final String format, final Object... arguments) {
        logger.debug(format, arguments);
    }

    @Override
    public void debug(final String message) {
        logger.debug(message);
    }

    @Override
    public void debug(final String message, final Throwable throwable) {
        logger.debug(message, throwable);
    }

    @Override
    public void error(final String message) {
        logger.error(message);
    }

    @Override
    public void error(final String message, final Throwable throwable) {
        logger.error(message, throwable);
    }

    @Override
    public void error(final String format, final Object... arguments) {
        logger.error(format, arguments);
    }

    @Override
    public void info(final String message) {
        logger.info(message);
    }

    @Override
    public void info(final String message, final Throwable throwable) {
        logger.info(message, throwable);
    }

    @Override
    public void info(final String format, final Object... arguments) {
        logger.info(format, arguments);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void warn(final String message) {
        logger.warn(message);
    }

    @Override
    public void warn(final String message, final Throwable throwable) {
        logger.warn(message, throwable);
    }

    @Override
    public void warn(final String format, final Object... arguments) {
        logger.warn(format, arguments);
    }
}
