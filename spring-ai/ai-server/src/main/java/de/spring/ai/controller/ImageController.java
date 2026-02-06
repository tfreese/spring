package de.spring.ai.controller;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import de.spring.ai.Utils;
import de.spring.ai.tools.ImageTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("image")
public class ImageController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageController.class);

    private record TextAnswer(
            String answer,          // Answer for User
            boolean generateImage,  // true => Generate Image
            String imagePrompt      // Image prompt if generateImage=true
    ) {
    }

    private final ChatClient chatClient;
    private final ImageTool imageTool;

    public ImageController(final ChatClient.Builder chatClientBuilder, final ImageTool imageTool) {
        super();

        this.chatClient = chatClientBuilder
                .clone()
                .build();

        this.imageTool = Objects.requireNonNull(imageTool, "imageTool required");
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(value = "prompt") final String prompt) {
        LOGGER.info("Execute Prompt: {}", prompt);

        final LocalDateTime start = LocalDateTime.now();

        final ResponseEntity<ChatResponse, TextAnswer> responseEntity = chatClient.prompt(
                        """
                                Du bist ein hilfreicher Assistent.
                                Antworte klar und professional auf die Nutzerfrage in der Sprache deines Konversationspartners.
                                
                                Nur wenn der Nutzer ein Bild, Visualisierung, Illustration oder Diagramm haben möchte,
                                setzte "generateImage" auf true und erzeuge einen sinnvollen ImagePrompt.
                                
                                Liefere einen strukturierten Output mit Feldern:
                                - answer: String (deine Text-Antwort für den Nutzer)
                                - generateImage: boolean (ob ein Bild generiert werden soll)
                                - imagePrompt: String (nur setzen, wenn generateImage=true; klare Bildbeschreibung)
                                """
                )
                .user(prompt)
                .call()
                // .entity(TextAnswer.class)
                .responseEntity(TextAnswer.class);

        final TextAnswer textAnswer = responseEntity.entity();

        if (textAnswer == null) {
            return "no answer";
        }

        final String image;

        if (textAnswer.generateImage() && textAnswer.imagePrompt() != null && !textAnswer.imagePrompt().isBlank()) {
            image = imageTool.generateImage(textAnswer.imagePrompt());
        } else {
            image = null;
        }

        final Usage usage = Optional.ofNullable(responseEntity.getResponse()).map(ChatResponse::getMetadata).map(ChatResponseMetadata::getUsage).orElse(null);

        return Utils.toHtml(prompt, start, usage, sb -> {
            sb.append(textAnswer.answer());

            if (image != null) {
                sb.append("<br>");
                sb.append("<br>");
                sb.append("<b>Image prompt:</b><br>");
                sb.append(textAnswer.imagePrompt());
                sb.append("<br>");
                sb.append("<img src='data:image/png;base64,").append(image).append("'/>");
            }
        });
    }

    @GetMapping
    public String image(@RequestParam(value = "prompt") final String prompt) {
        LOGGER.info("Execute Prompt: {}", prompt);

        final LocalDateTime start = LocalDateTime.now();

        final String image = imageTool.generateImage(prompt);

        if (image == null) {
            return "No image created!";
        }

        if (image.startsWith("http")) {
            return Utils.toHtml(prompt, start, null, sb -> sb.append("<a href=\"").append(image).append("\" target=\"_blank\">").append(image).append("/a>"));
        }

        // b64_json
        return Utils.toHtml(prompt, start, null, sb -> sb.append("<img src='data:image/png;base64,").append(image).append("'/>"));
    }
}
