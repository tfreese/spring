package com.example.spring_ai_demo;

import java.util.Optional;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    private final ChatClient chatClient;
    private final ImageModel imageModel;

    public ChatController(final ChatClient chatClient, final ImageModel imageModel) {
        super();

        this.chatClient = chatClient;
        this.imageModel = imageModel;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody final String prompt) {
        return chatClient.prompt().user(prompt).call().content();
    }

    @PostMapping("/image")
    public String generateImage(@RequestBody final String prompt) {
        final ImagePrompt imagePrompt = new ImagePrompt(prompt);
        final ImageResponse imageResponse = imageModel.call(imagePrompt);
        final ImageGeneration imageGeneration = imageResponse.getResult();
        final Image image = imageGeneration.getOutput();

        return Optional.ofNullable(image.getUrl()).orElseGet(image::getB64Json);
    }
}
