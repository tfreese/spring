package de.spring.ai.tools;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
public class ImageTool {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageTool.class);

    private final ImageModel imageModel;

    public ImageTool(final ImageModel imageModel) {
        super();

        this.imageModel = imageModel;
    }

    /**
     * Return the URL or "b64_json" Format from the Picture.
     */
    // @Tool(description = """
    //         Generate an image from a short description.
    //         """
    //         , returnDirect = true // Do not return Result to Model, the "b64_json" Format will cause a Context-Window overflow.
    // )
    public @Nullable String generateImage(@ToolParam(description = "Short description of the image") final String description) {
        LOGGER.info("Generating an image.");

        // Overwrite defaults.
        // final ImageOptions imageOptions = ImageOptionsBuilder.builder()
        //         .model("gpt-image-1")
        //         .height(1024)
        //         .width(1024)
        //         .style("vivid")
        //         .responseFormat("b64_json")
        //         .N(1)
        //         .build();

        // final ImagePrompt imagePrompt = new ImagePrompt(description, imageOptions);
        final ImagePrompt imagePrompt = new ImagePrompt(description);
        final ImageResponse imageResponse = imageModel.call(imagePrompt);
        final ImageGeneration imageGeneration = imageResponse.getResult();

        if (imageGeneration == null) {
            return null;
        }

        final Image image = imageGeneration.getOutput();

        return Optional.ofNullable(image.getUrl()).orElse(image.getB64Json());
    }
}
