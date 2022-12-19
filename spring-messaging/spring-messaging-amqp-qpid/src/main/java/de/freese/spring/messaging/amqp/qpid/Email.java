// Created: 31.01.2019
package de.freese.spring.messaging.amqp.qpid;

/**
 * @author Thomas Freese
 */
public record Email(String to, String body)
{
}
