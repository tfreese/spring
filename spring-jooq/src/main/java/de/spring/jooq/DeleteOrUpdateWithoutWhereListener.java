// Created: 22.05.2026
package de.spring.jooq;

import java.io.Serial;
import java.util.regex.Pattern;

import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;

/**
 * <a href="https://www.jooq.org/doc/latest/manual/sql-execution/execute-listeners>execute-listeners</a>
 *
 * @author Thomas Freese
 */
public final class DeleteOrUpdateWithoutWhereListener implements ExecuteListener {
    /**
     * Origin: "^(?i:(UPDATE|DELETE)(?!.* WHERE ).*)$"
     */
    private static final Pattern PATTERN = Pattern.compile("^(update|delete)(?!.* where ).*");

    @Serial
    private static final long serialVersionUID = 65668910022673101L;

    public static final class DeleteOrUpdateWithoutWhereException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 4010783381888679891L;

        private DeleteOrUpdateWithoutWhereException(final String message) {
            super(message);
        }
    }

    @Override
    public void renderEnd(final ExecuteContext ctx) {
        final String sql = ctx.sql();

        if (sql != null && PATTERN.matcher(sql).matches()) {
            throw new DeleteOrUpdateWithoutWhereException(sql);
        }
    }
}
