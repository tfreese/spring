package de.spring.ai.tools.sql.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Thomas Freese
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record ColumnInfo(String name,
                         String dataType,
                         String description) {

}
