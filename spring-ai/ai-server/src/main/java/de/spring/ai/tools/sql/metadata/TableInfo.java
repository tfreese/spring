package de.spring.ai.tools.sql.metadata;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Thomas Freese
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record TableInfo(
        String name,
        String description,
        String catalog,
        String schema,
        List<ColumnInfo> columns) {

}
