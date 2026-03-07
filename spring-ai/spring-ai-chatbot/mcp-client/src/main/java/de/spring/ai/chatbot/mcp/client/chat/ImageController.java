package de.spring.ai.chatbot.mcp.client.chat;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <b>DOES NOT WORK WITH OLLAMA!</b><br>
 * <a href="http://localhost:8082/image?prompt=erzeuge ein bild mit einer rose">create rose</a>
 */
// @RestController("image")
public class ImageController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageController.class);

    private final ImageModel imageModel;

    public ImageController(final ImageModel imageModel) {
        super();

        this.imageModel = Objects.requireNonNull(imageModel, "imageModel required");
    }

    @GetMapping
    public String generateImage(@RequestParam(value = "prompt") final String prompt) {
        LOGGER.info("Execute Prompt: {}", prompt);

        final ImagePrompt imagePrompt = new ImagePrompt(prompt);
        final ImageResponse imageResponse = imageModel.call(imagePrompt);
        final ImageGeneration imageGeneration = imageResponse.getResult();
        final Image image = imageGeneration.getOutput();

        return Optional.ofNullable(image.getUrl()).orElseGet(image::getB64Json);
    }
}
