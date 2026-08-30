package de.freese.spring.javafx;

import javafx.application.Application;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Thomas Freese
 * @since 09.02.2019
 */
@SpringBootApplication
// @SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public final class JavaFxApplicationLauncher {
    static void main(final String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }

    private JavaFxApplicationLauncher() {
        super();
    }
}
