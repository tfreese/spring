package de.spring.ai.tools.sql;

/**
 * Response of SQL query.
 *
 * @param result Success result
 * @param error  Error result
 * @author Thomas Freese
 */
public record RunSqlQueryResponse(String result, String error) {
}
