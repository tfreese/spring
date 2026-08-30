package de.freese.spring.messaging.jms;

/**
 * @author Thomas Freese
 * @since 31.01.2019
 */
public record Email(String to, String body) {
}
