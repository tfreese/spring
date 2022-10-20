// Created: 09.02.2019
package de.freese.spring.javafx;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
class MainSpringFxApplication
{
    public static void main(final String[] args)
    {
        Application.launch(JavaFxApplication.class, args);
    }
}
