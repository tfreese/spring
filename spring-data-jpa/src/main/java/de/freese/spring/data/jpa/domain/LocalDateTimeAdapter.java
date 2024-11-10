// Created: 29.01.24
package de.freese.spring.data.jpa.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Thomas Freese
 */
public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
    @Override
    public String marshal(final LocalDateTime localDateTime) {
        return localDateTime != null ? DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime) : null;
    }

    @Override
    public LocalDateTime unmarshal(final String s) {
        return s != null ? DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(s, LocalDateTime::from) : null;
    }
}
