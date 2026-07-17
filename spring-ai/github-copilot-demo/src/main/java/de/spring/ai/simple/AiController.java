// Created: 17.07.2026
package de.spring.ai.simple;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
public class AiController {

    private final ChatClient chatClient;

    public AiController(final ChatClient.Builder chatClientBuilder) {
        super();

        chatClient = chatClientBuilder
                .defaultSystem("Du bist ein hilfreicher Assistent für Java und Spring.")
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @GetMapping("/ai/generate")
    public String generate(@RequestParam(value = "message", defaultValue = "Erzähle mir einen kurzen Entwickler-Witz.") final String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();

        // return chatClient.prompt()
        //         .user(message)
        //         .call()
        //         .responseEntity(String.class)
        //         .entity();
    }
}