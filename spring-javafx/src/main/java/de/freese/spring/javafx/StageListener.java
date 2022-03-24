// Created: 09.02.2019
package de.freese.spring.javafx;

import java.io.IOException;
import java.net.URL;

import de.freese.spring.javafx.JavaFxApplication.StageReadyEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
public class StageListener implements ApplicationListener<StageReadyEvent>
{
    /**
     *
     */
    private final ApplicationContext applicationContext;
    /**
     *
     */
    private final String applicationTitle;
    /**
     *
     */
    private final Resource fxml;

    /**
     * Erstellt ein neues {@link StageListener} Object.
     *
     * @param applicationContext {@link ApplicationContext}
     * @param applicationTitle String
     * @param fxml {@link Resource}
     */
    public StageListener(final ApplicationContext applicationContext, @Value("${spring.application.ui.title}") final String applicationTitle,
                         @Value("classpath:/ui.fxml") final Resource fxml)
    {
        super();

        this.applicationContext = applicationContext;
        this.applicationTitle = applicationTitle;
        this.fxml = fxml;
    }

    /**
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(final StageReadyEvent event)
    {
        try
        {
            Stage stage = event.getStage();
            URL url = this.fxml.getURL();

            FXMLLoader fxmlLoader = new FXMLLoader(url);
            fxmlLoader.setControllerFactory(this.applicationContext::getBean);

            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 600, 600);
            stage.setScene(scene);
            stage.setTitle(this.applicationTitle);
            stage.show();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
