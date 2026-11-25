package de.spring.angular.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record Task(
        long id,
        String title,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        LocalDateTime createdAt
) {
}

