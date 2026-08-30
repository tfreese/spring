package de.freese.spring.messaging.amqp.qpid;

/**
 * @author Thomas Freese
 * @since 31.01.2019
 */
public record Email(String to, String body) {
}
