// Created: 09.02.2019
package de.freese.spring.javafx;

import java.util.Objects;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
public class UiController // implements javafx.fxml.Initializable
{
    private final HostServices hostServices;

    @FXML
    private Button button;

    @FXML
    private Label label;

    public UiController(final HostServices hostServices) {
        super();

        this.hostServices = Objects.requireNonNull(hostServices, "hostServices required");
    }

    @FXML
    public void initialize() {
        button.setOnAction(event -> label.setText(hostServices.getDocumentBase()));
    }
}
