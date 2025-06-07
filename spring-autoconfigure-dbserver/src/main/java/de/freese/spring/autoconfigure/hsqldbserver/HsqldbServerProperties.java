// Created: 13.02.2017
package de.freese.spring.autoconfigure.hsqldbserver;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for {@link HsqldbServerAutoConfiguration}.<br>
 *
 * @author Thomas Freese
 */
@ConfigurationProperties(prefix = "hsqldb.server")
public class HsqldbServerProperties {
    /**
     * @author Thomas Freese
     */
    public static class DB {
        private String name;
        private String path;

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setPath(final String path) {
            this.path = path;
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("DB [name=").append(name);
            builder.append(", path=").append(path);
            builder.append("]");

            return builder.toString();
        }
    }

    private List<DB> db;
    private boolean enabled = true;
    private boolean noSystemExit = true;
    private int port;
    private boolean silent = true;
    private boolean trace;

    public List<DB> getDb() {
        return db;
    }

    public int getPort() {
        return port;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isNoSystemExit() {
        return noSystemExit;
    }

    public boolean isSilent() {
        return silent;
    }

    public boolean isTrace() {
        return trace;
    }

    public void setDb(final List<DB> db) {
        this.db = db;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void setNoSystemExit(final boolean noSystemExit) {
        this.noSystemExit = noSystemExit;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public void setSilent(final boolean silent) {
        this.silent = silent;
    }

    public void setTrace(final boolean trace) {
        this.trace = trace;
    }
}
