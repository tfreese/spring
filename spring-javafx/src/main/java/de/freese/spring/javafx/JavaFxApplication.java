// Created: 09.02.2019
package de.freese.spring.javafx;

import java.io.Serial;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author Thomas Freese
 */
public class JavaFxApplication extends Application
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaFxApplication.class);

    /**
     * @author Thomas Freese
     */
    public static class StageReadyEvent extends ApplicationEvent
    {
        @Serial
        private static final long serialVersionUID = -4583752880347446992L;

        public StageReadyEvent(final Stage source)
        {
            super(source);
        }

        public Stage getStage()
        {
            return (Stage) getSource();
        }
    }

    private ConfigurableApplicationContext applicationContext;

    /**
     * @see javafx.application.Application#init()
     */
    @Override
    public void init() throws Exception
    {
        ApplicationContextInitializer<GenericApplicationContext> initializer = ac ->
        {
            ac.registerBean(Application.class, () -> JavaFxApplication.this);
            ac.registerBean(Parameters.class, this::getParameters);
            ac.registerBean(HostServices.class, this::getHostServices);

            ac.addApplicationListener((ApplicationListener<ContextClosedEvent>) (event -> LOGGER.info("Closing ApplicationContext")));
        };

        // @formatter:off
        this.applicationContext = new SpringApplicationBuilder()
                .sources(JavaFxApplicationLauncher.class)
                .initializers(initializer)
                //.registerShutdownHook(true)
                .run(getParameters().getRaw().toArray(new String[0]));
        // @formatter:on
    }

    /**
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(final Stage primaryStage) throws Exception
    {
        this.applicationContext.publishEvent(new StageReadyEvent(primaryStage));
    }

    /**
     * @see javafx.application.Application#stop()
     */
    @Override
    public void stop() throws Exception
    {
        this.applicationContext.close();
        Platform.exit();
    }
}
