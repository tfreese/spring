package com.spring.ai.ollama.controller;

import jakarta.annotation.Resource;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

@RestController
@RequestMapping("/ai/chat")
public class ChatController {

    @Resource
    private ChatClient chatClient;

    @GetMapping("/")
    public String chat(@RequestParam(value = "message") final String message) {
        return chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, RequestContextHolder.currentRequestAttributes().getSessionId()))
                .user(message)
                .call()
                .content();
    }
}
