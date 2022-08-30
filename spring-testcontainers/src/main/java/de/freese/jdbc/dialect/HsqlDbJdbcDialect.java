package de.freese.jdbc.dialect;

/**
 * @author Thomas Freese
 */
class HsqlDbJdbcDialect implements JdbcDialect
{
    @Override
    public String getSelectSequenceNextValString(final String name)
    {
        return "call next value for %s".formatted(name);
    }
}
