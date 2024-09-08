package ma.insea.connect.chatbot.service;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ma.insea.connect.chat.common.chatMessage.DTO.ChatMessageDTO;
import ma.insea.connect.chat.common.chatMessage.service.ChatMessageService;
import ma.insea.connect.chat.common.chatMessage.DTO.GroupMessageDTO;
import ma.insea.connect.chatbot.DTO.conversationDTO.ChatbotMessageRequestDTO;
import ma.insea.connect.chatbot.DTO.conversationDTO.ChatbotApiResponseDTO;
import ma.insea.connect.chatbot.DTO.conversationDTO.ChatbotApiRequestDTO;
import ma.insea.connect.chatbot.DTO.conversationDTO.ChatbotMessageResponseDTO;
import ma.insea.connect.chatbot.DTO.CreateThreadDTO;
import ma.insea.connect.chatbot.DTO.groupDTO.ChatbotGroupMessageRequestDTO;
import ma.insea.connect.chatbot.DTO.groupDTO.ChatbotGroupMessageResponseDTO;
import ma.insea.connect.exception.ChatBotException.ChatbotApiException;
import ma.insea.connect.exception.ChatBotException.GroupMessageProcessingException;
import ma.insea.connect.exception.ChatBotException.MessageProcessingException;
import ma.insea.connect.exception.ChatBotException.ThreadCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@Slf4j
@Service
public class ChatbotService {

    @Autowired
    ChatMessageService chatMessageService;

    @Value("${chatbotServer}")
    private String chatbotServer;

    private final long chatbotId = 1L;
    private final RestTemplate restTemplate = new RestTemplate();

    private String cleanResponseMessage(String message) {
        String regex = "【\\d+:\\d+†source】";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        log.info("Formatted text without crosses " + matcher.replaceAll(""));
        return matcher.replaceAll("");
    }

    // Method to stream conversation messages using SseEmitter
    public SseEmitter sendToBotConversationStream(ChatbotMessageRequestDTO requestDTO) {
        SseEmitter emitter = new SseEmitter();

        new Thread(() -> {
            try {
                // Get the full response from the chatbot service
                ChatbotMessageResponseDTO responseDTO = sendToBotConversation(requestDTO);

                // Clean the response message
                String cleanedMessage = cleanResponseMessage(responseDTO.getMessage());

                // Stream the response word by word
                String[] words = cleanedMessage.split(" ");
                for (String word : words) {
                    emitter.send(SseEmitter.event().data(word));
                    Thread.sleep(150); // Adjust delay as needed
                }
                emitter.complete();
            } catch (IOException e) {
                log.error("Error during message streaming", e);
                emitter.completeWithError(new MessageProcessingException("Failed to stream the message: " + e.getMessage()));
            } catch (Exception e) {
                log.error("Unexpected error during message streaming", e);
                emitter.completeWithError(new MessageProcessingException("Unexpected error: " + e.getMessage()));
            }
        }).start();

        return emitter;
    }

    // Method to handle interactions in a conversation
    public ChatbotMessageResponseDTO addInteractionInConversation(ChatbotMessageRequestDTO chatbotMessageRequestDTO, String responseContent) {
        try {
            Date date = new Date();
            ChatMessageDTO userRequest = new ChatMessageDTO();
            userRequest.setContent(chatbotMessageRequestDTO.getContent());
            userRequest.setTimestamp(chatbotMessageRequestDTO.getTimestamp());
            userRequest.setSenderId(chatbotMessageRequestDTO.getSenderId());
            userRequest.setRecipientId(chatbotMessageRequestDTO.getRecipientId());

            ChatMessageDTO botResponse = new ChatMessageDTO();
            botResponse.setRecipientId(chatbotMessageRequestDTO.getSenderId());
            botResponse.setSenderId(chatbotMessageRequestDTO.getRecipientId());
            botResponse.setContent(cleanResponseMessage(responseContent));
            botResponse.setTimestamp(date);

            chatMessageService.saveusermessage(userRequest);
            chatMessageService.saveusermessage(botResponse);

            ChatbotMessageResponseDTO chatbotMessageResponseDTO = new ChatbotMessageResponseDTO();
            chatbotMessageResponseDTO.setMessage(botResponse.getContent());
            return chatbotMessageResponseDTO;
        } catch (Exception e) {
            log.error("Error processing interaction in conversation", e);
            throw new MessageProcessingException("Failed to process interaction in conversation: " + e.getMessage());
        }
    }

    // Method to handle interactions in a group
    public ChatbotGroupMessageResponseDTO addInteractionToGroup(ChatbotGroupMessageRequestDTO chatbotGroupMessageRequestDTO, String responseContent) {
        try {
            Date date = new Date();
            GroupMessageDTO userRequest = new GroupMessageDTO();
            userRequest.setContent(chatbotGroupMessageRequestDTO.getContent());
            userRequest.setTimestamp(chatbotGroupMessageRequestDTO.getTimestamp());
            userRequest.setSenderId(chatbotGroupMessageRequestDTO.getSenderId());
            userRequest.setGroupId(chatbotGroupMessageRequestDTO.getGroupId());

            GroupMessageDTO botResponse = new GroupMessageDTO();
            botResponse.setGroupId(chatbotGroupMessageRequestDTO.getGroupId());
            botResponse.setSenderId(chatbotId);
            botResponse.setContent(cleanResponseMessage(responseContent));
            botResponse.setTimestamp(date);

            chatMessageService.savegroupmessage(userRequest);
            chatMessageService.savegroupmessage(botResponse);

            ChatbotGroupMessageResponseDTO chatbotGroupMessageResponseDTO = new ChatbotGroupMessageResponseDTO();
            chatbotGroupMessageResponseDTO.setMessage(botResponse.getContent());
            return chatbotGroupMessageResponseDTO;
        } catch (Exception e) {
            log.error("Error processing interaction in group", e);
            throw new GroupMessageProcessingException("Failed to process interaction in group: " + e.getMessage());
        }
    }

    // Method to retrieve a thread ID as a string
    public String getThreadIdString() {
        String url = chatbotServer + "/start_conversation";
        HttpEntity<String> request = new HttpEntity<>(new String());

        try {
            ResponseEntity<CreateThreadDTO> response = restTemplate.postForEntity(url, request, CreateThreadDTO.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                CreateThreadDTO createThreadDTO = response.getBody();
                if (createThreadDTO != null && createThreadDTO.getThread_id() != null) {
                    return createThreadDTO.getThread_id();
                } else {
                    throw new ThreadCreationException("Failed to create thread. Response body is missing thread_id.");
                }
            } else {
                throw new ThreadCreationException("Error creating thread: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error retrieving thread ID", e);
            throw new ThreadCreationException("Unexpected error while creating thread: " + e.getMessage());
        }
    }

    // Method to redirect API requests to the chatbot server
    public ChatbotApiResponseDTO redirect(ChatbotApiRequestDTO requestDTO) {
        String url = chatbotServer + "/process_request";
        HttpEntity<ChatbotApiRequestDTO> requestEntity = new HttpEntity<>(requestDTO);

        try {
            ResponseEntity<ChatbotApiResponseDTO> responseEntity = restTemplate.postForEntity(url, requestEntity, ChatbotApiResponseDTO.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return responseEntity.getBody();
            } else {
                throw new ChatbotApiException("Failed to process request at chatbot server. Status: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error redirecting API request to chatbot server", e);
            throw new ChatbotApiException("Unexpected error while processing chatbot request: " + e.getMessage());
        }
    }

    // Method to send a message to the chatbot conversation
    public ChatbotMessageResponseDTO sendToBotConversation(ChatbotMessageRequestDTO chatbotMessageRequestDTO) {
        try {
            ChatbotApiRequestDTO chatbotApiRequestDTO = new ChatbotApiRequestDTO(chatbotMessageRequestDTO.getThreadId(), chatbotMessageRequestDTO.getContent());
            ChatbotApiResponseDTO responseFromApi = redirect(chatbotApiRequestDTO);
            return addInteractionInConversation(chatbotMessageRequestDTO, responseFromApi.getMessage());
        } catch (Exception e) {
            log.error("Error sending message to chatbot conversation", e);
            throw new MessageProcessingException("Failed to send message to chatbot conversation: " + e.getMessage());
        }
    }

    // Method to send a message to a chatbot group
    public ChatbotGroupMessageResponseDTO sendToBotGroup(ChatbotGroupMessageRequestDTO chatbotGroupMessageRequestDTO) {
        try {
            ChatbotApiRequestDTO chatbotApiRequestDTO = new ChatbotApiRequestDTO(chatbotGroupMessageRequestDTO.getThreadId(), chatbotGroupMessageRequestDTO.getContent());
            ChatbotApiResponseDTO responseFromApi = redirect(chatbotApiRequestDTO);
            return addInteractionToGroup(chatbotGroupMessageRequestDTO, responseFromApi.getMessage());
        } catch (Exception e) {
            log.error("Error sending message to chatbot group", e);
            throw new GroupMessageProcessingException("Failed to send message to chatbot group: " + e.getMessage());
        }
    }
}


