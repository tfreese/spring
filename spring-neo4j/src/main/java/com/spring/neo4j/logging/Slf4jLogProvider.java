// Created: 30.06.2025
package com.spring.neo4j.logging;

import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Freese
 */
public final class Slf4jLogProvider implements LogProvider {
    @Override
    public Log getLog(final Class<?> loggingClass) {
        return new Slf4jLog(LoggerFactory.getLogger(loggingClass));
    }

    @Override
    public Log getLog(final String name) {
        return new Slf4jLog(LoggerFactory.getLogger(name));
    }
}
