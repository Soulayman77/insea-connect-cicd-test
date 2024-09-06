package ma.insea.connect.chat.conversation.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Collections;

import ma.insea.connect.chat.conversation.DTO.ConversationDTO;
import ma.insea.connect.chat.conversation.DTO.ConversationDTO2;
import ma.insea.connect.chat.conversation.model.Conversation;
import ma.insea.connect.chat.conversation.repository.ConversationRepository;
import ma.insea.connect.exception.ChatException.ConversationNotFoundException;
import ma.insea.connect.exception.ChatException.UnauthorizedConversationAccessException;
import ma.insea.connect.exception.userExceptions.UnauthorizedAccessException;
import ma.insea.connect.exception.userExceptions.UserNotFoundException;
import org.springframework.stereotype.Service;

import ma.insea.connect.chat.common.chatMessage.service.ChatMessageService;
import ma.insea.connect.user.DTO.User;
import ma.insea.connect.user.DTO.UserDTO2;
import ma.insea.connect.user.repository.UserRepository;
import ma.insea.connect.utils.Functions;
import ma.insea.connect.chat.common.chatMessage.model.ChatMessage;
import ma.insea.connect.chat.common.chatMessage.DTO.ChatMessageDTO;
import ma.insea.connect.chat.common.chatMessage.DTO.ChatMessageDTO2;
import ma.insea.connect.chat.common.chatMessage.repository.ChatMessageRepository;


import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ChatMessageService chatMessageService;
    private final ChatMessageRepository chatMessageRepository;
    private final Functions functions;

    public List<ConversationDTO> findConversationsByEmail() {
        User connectedUser = functions.getConnectedUser();
        String email = connectedUser.getEmail();

        User user2 = userRepository.findByEmail(email);
        if (user2 == null) {
            throw new UserNotFoundException("User with email " + email + " not found.");
        }
        System.out.println("useer" + user2);
        List<Conversation> conversations = conversationRepository.findAllByMember1OrMember2(user2, user2);

        List<ConversationDTO> conversationDTOs = new ArrayList<>();
        for (Conversation conversation : conversations) {
            ChatMessageDTO chatMessage = chatMessageService.findLastMessage(conversation.getChatId());

            String member1 = conversation.getMember1().getEmail();
            String member2 = conversation.getMember2().getEmail();
            String recipientId = member1.equals(email) ? member2 : member1;
            User user = userRepository.findByEmail(recipientId);
            if (user == null) {
                throw new UserNotFoundException("Recipient with email " + recipientId + " not found.");
            }

            ConversationDTO conversationDTO = new ConversationDTO();
            conversationDTO.setChatId(conversation.getChatId());
            conversationDTO.setUsername(user.getUsername());
            conversationDTO.setLastLogin(user.getLastLogin());
            conversationDTO.setStatus(user.getStatus());
            conversationDTO.setLastMessage(chatMessage);

            conversationDTOs.add(conversationDTO);
        }

        Collections.reverse(conversationDTOs);
        conversationDTOs.sort(Comparator.comparing(
                        (ConversationDTO conversationDTO) ->
                                conversationDTO.getLastMessage() != null ? conversationDTO.getLastMessage().getTimestamp() : new Date(0)
                ).reversed()
        );


        return conversationDTOs;
    }

    public List<ConversationDTO> findConversationsByID(Long myId) {
        User user2 = userRepository.findById(myId)
                .orElseThrow(() -> new UserNotFoundException(myId));
        List<Conversation> conversations = conversationRepository.findAllByMember1OrMember2(user2, user2);

        List<ConversationDTO> conversationDTOs = new ArrayList<>();
        for (Conversation conversation : conversations) {
            ChatMessageDTO chatMessage = chatMessageService.findLastMessage(conversation.getChatId());

            Long member1 = conversation.getMember1().getId();
            Long member2 = conversation.getMember2().getId();
            Long recipientId = member1.equals(myId) ? member2 : member1;
            User user = userRepository.findById(recipientId)
                    .orElseThrow(() -> new UserNotFoundException(recipientId));

            ConversationDTO conversationDTO = new ConversationDTO();
            conversationDTO.setChatId(conversation.getChatId());
            conversationDTO.setRecipientId(recipientId);
            conversationDTO.setUsername(user.getUsername());
            conversationDTO.setLastLogin(user.getLastLogin());
            conversationDTO.setStatus(user.getStatus());
            conversationDTO.setLastMessage(chatMessage);

            conversationDTOs.add(conversationDTO);
        }
        return conversationDTOs;
    }

    public List<ChatMessageDTO2> findConversationMessages(String conversationId) {
        User connectedUser = functions.getConnectedUser();
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatId(conversationId);

        Conversation conversation = conversationRepository.findByChatId(conversationId);
        if (conversation == null) {
            throw new ConversationNotFoundException(conversationId);
        }

        User user1 = conversation.getMember1();
        User user2 = conversation.getMember2();

        if (!connectedUser.equals(user1) && !connectedUser.equals(user2)) {
            throw new UnauthorizedAccessException("User is not authorized to access this conversation.");
        }

        List<ChatMessageDTO2> chatMessageDTOs = new ArrayList<>();
        for (ChatMessage chatMessage : chatMessages) {
            ChatMessageDTO2 chatMessageDTO = new ChatMessageDTO2();
            chatMessageDTO.setContent(chatMessage.getContent());
            chatMessageDTO.setTimestamp(chatMessage.getTimestamp());
            chatMessageDTO.setSenderId(chatMessage.getSender().getId());
            chatMessageDTO.setSenderName(chatMessage.getSender().getUsername());
            chatMessageDTOs.add(chatMessageDTO);
        }

        return chatMessageDTOs;
    }

    public ConversationDTO2 getConversation(String conversationId) {
        User connectedUser = functions.getConnectedUser();
        Conversation conversation = conversationRepository.findByChatId(conversationId);
        if (conversation == null) {
            throw new ConversationNotFoundException(conversationId);
        }

        User user1 = conversation.getMember1();
        User user2 = conversation.getMember2();

        if (!connectedUser.equals(user1) && !connectedUser.equals(user2)) {
            throw new UnauthorizedAccessException("User is not authorized to access this conversation.");
        }

        UserDTO2 userDTO1 = new UserDTO2(user1.getId(), user1.getUsername(), user1.getEmail());
        UserDTO2 userDTO2 = new UserDTO2(user2.getId(), user2.getUsername(), user2.getEmail());

        return new ConversationDTO2(userDTO1, userDTO2);
    }

    public Conversation createConversation(Long long1) {
        User connectedUser = functions.getConnectedUser();
        User user2 = userRepository.findById(long1)
                .orElseThrow(() -> new UserNotFoundException(long1));
        String chatId = chatMessageService.getChatRoomId(connectedUser.getId().toString(), user2.getId().toString(), true);
        Conversation conversation = new Conversation();
        conversation.setChatId(chatId);
        conversation.setMember1(connectedUser);
        conversation.setMember2(user2);
        conversationRepository.save(conversation);
        return conversation;
    }
}