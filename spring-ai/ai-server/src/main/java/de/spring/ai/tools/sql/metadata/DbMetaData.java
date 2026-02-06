package de.spring.ai.tools.sql.metadata;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Thomas Freese
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record DbMetaData(List<TableInfo> tables) {

}
