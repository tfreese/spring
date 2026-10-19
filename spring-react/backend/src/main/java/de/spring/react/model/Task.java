package de.spring.react.model;

import java.time.LocalDateTime;

/**
 * Zeitformate müssen "yyyy-MM-dd'T'HH:mm" sein, damit sie korrekt in der React-App angezeigt werden.<br/>
 * Ansonsten müsste über eine eigene Funktion umformatiert werden.
 */
public record Task(long id,
                   String title,
                   @IsoDateTime
                   LocalDateTime createdAt
) {
}
