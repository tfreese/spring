/**
 * Created: 09.02.2019
 */
package de.freese.spring.javafx;

import java.util.Objects;
import org.springframework.stereotype.Component;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * @author Thomas Freese
 */
@Component
public class UiController // implements javafx.fxml.Initializable
{
    /**
     *
     */
    @FXML
    private Button button;

    /**
     *
     */
    private final HostServices hostServices;

    /**
     *
     */
    @FXML
    private Label label;

    /**
     * Erstellt ein neues {@link UiController} Object.
     *
     * @param hostServices {@link HostServices}
     */
    public UiController(final HostServices hostServices)
    {
        super();

        this.hostServices = Objects.requireNonNull(hostServices, "hostServices requred");
    }

    /**
     *
     */
    @FXML
    public void initialize()
    {
        this.button.setOnAction(event -> this.label.setText(this.hostServices.getDocumentBase()));
    }
}
