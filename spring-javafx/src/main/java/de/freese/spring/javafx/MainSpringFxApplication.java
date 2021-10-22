// Created: 09.02.2019
package de.freese.spring.javafx;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import javafx.application.Application;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public class MainSpringFxApplication
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        Application.launch(JavaFxApplication.class, args);
    }
}
