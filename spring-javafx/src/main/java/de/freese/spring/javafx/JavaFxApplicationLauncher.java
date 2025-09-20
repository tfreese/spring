// Created: 09.02.2019
package de.freese.spring.javafx;

import javafx.application.Application;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class JavaFxApplicationLauncher {
    static void main(final String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }
}
