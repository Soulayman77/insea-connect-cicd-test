package ma.insea.connect.chatbot.controller;


import lombok.extern.slf4j.Slf4j;
import ma.insea.connect.chatbot.DTO.groupDTO.ChatbotGroupMessageRequestDTO;
import ma.insea.connect.chatbot.DTO.groupDTO.ChatbotGroupMessageResponseDTO;
import ma.insea.connect.chatbot.service.ChatbotService;
import ma.insea.connect.chatbot.DTO.conversationDTO.ChatbotMessageRequestDTO;
import ma.insea.connect.chatbot.DTO.conversationDTO.ChatbotMessageResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@RestController
@RequestMapping("api/v1/chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/sendMessage/conversation/stream")
    public SseEmitter sendToBotConversationStream(@RequestBody ChatbotMessageRequestDTO requestDTO) {
        return chatbotService.sendToBotConversationStream(requestDTO);
    }

    @PostMapping("/sendMessage/conversation")
    public ResponseEntity<ChatbotMessageResponseDTO> sendToBotConversation(@RequestBody ChatbotMessageRequestDTO requestDTO) {
        try {
            ChatbotMessageResponseDTO chatbotMessageResponseDTO = chatbotService.sendToBotConversation(requestDTO);
            return ResponseEntity.ok(chatbotMessageResponseDTO);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage(), e);
        }
    }

    @PostMapping("/sendMessage/group")
    public ResponseEntity<ChatbotGroupMessageResponseDTO> sendToBotGroup(@RequestBody ChatbotGroupMessageRequestDTO requestDTO) {
        try {
            ChatbotGroupMessageResponseDTO chatbotGroupMessageResponseDTO = chatbotService.sendToBotGroup(requestDTO);
            return ResponseEntity.ok(chatbotGroupMessageResponseDTO);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage(), e);
        }
    }
}
