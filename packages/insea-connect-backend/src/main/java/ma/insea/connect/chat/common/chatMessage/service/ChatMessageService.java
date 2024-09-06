package ma.insea.connect.chat.common.chatMessage.service;

import lombok.AllArgsConstructor;

import ma.insea.connect.chat.common.chatMessage.model.ChatMessage;
import ma.insea.connect.chat.common.chatMessage.DTO.*;
import ma.insea.connect.chat.common.chatMessage.model.GroupMessage;
import ma.insea.connect.chat.common.chatMessage.repository.ChatMessageRepository;
import ma.insea.connect.chat.common.chatMessage.repository.GroupMessageRepository;
import ma.insea.connect.exception.ChatException.GroupMessageException;
import ma.insea.connect.exception.ChatException.GroupNotFoundException;
import ma.insea.connect.exception.userExceptions.UnauthorizedAccessException;
import ma.insea.connect.exception.userExceptions.UserNotFoundException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import ma.insea.connect.chat.group.model.Membership;
import ma.insea.connect.chat.group.repository.MembershipRepository;
import ma.insea.connect.user.DTO.User;
import ma.insea.connect.user.repository.UserRepository;
import ma.insea.connect.utils.Functions;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final GroupMessageRepository groupMessageRepository;
    private final Functions functions;
    private final MembershipRepository membershipRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Save a user message and handle potential exceptions
    public ChatMessage saveusermessage(ChatMessageDTO chatMessage) {
        ChatMessage chatMessage1 = new ChatMessage();
        var chatId = getChatRoomId(Long.toString(chatMessage.getSenderId()), Long.toString(chatMessage.getRecipientId()), true);
        chatMessage1.setChatId(chatId);

        User recipient = userRepository.findById(chatMessage.getRecipientId())
                .orElseThrow(() -> new UserNotFoundException(chatMessage.getRecipientId()));
        User sender = userRepository.findById(chatMessage.getSenderId())
                .orElseThrow(() -> new UserNotFoundException(chatMessage.getSenderId()));

        chatMessage1.setSender(sender);
        chatMessage1.setRecipient(recipient);
        chatMessage1.setContent(chatMessage.getContent());
        chatMessage1.setTimestamp(new java.sql.Date(System.currentTimeMillis()));

        chatMessageRepository.save(chatMessage1);

        messagingTemplate.convertAndSendToUser(
                chatId, "/queue/messages",
                new ChatMessageDTO2(
                        chatMessage1.getSender().getId(),
                        chatMessage1.getContent(),
                        new java.util.Date(System.currentTimeMillis()),
                        chatMessage1.getSender().getUsername())
        );
        return chatMessage1;
    }

    // Save a group message and handle potential exceptions
    public GroupMessage savegroupmessage(GroupMessageDTO groupMessageDTO) {
        User sender = userRepository.findById(groupMessageDTO.getSenderId())
                .orElseThrow(() -> new UserNotFoundException(groupMessageDTO.getSenderId()));
        GroupMessage groupMessage = new GroupMessage();
        groupMessage.setSender(sender);
        groupMessage.setContent(groupMessageDTO.getContent());
        groupMessage.setGroupId(groupMessageDTO.getGroupId());
        groupMessage.setTimestamp(new java.sql.Date(System.currentTimeMillis()));

        groupMessageRepository.save(groupMessage);

        messagingTemplate.convertAndSendToUser(
                Long.toString(groupMessageDTO.getGroupId()), "/queue/messages",
                new GroupMessageDTO(
                        groupMessage.getSender().getId(),
                        groupMessage.getContent(),
                        groupMessage.getGroupId(),
                        groupMessage.getSender().getUsername(),
                        new Date(System.currentTimeMillis())
                )
        );
        return groupMessage;
    }

    public List<ChatMessage> findChatMessages(String senderId, String recipientId) {
        var chatId = getChatRoomId(senderId, recipientId, true);
        return chatMessageRepository.findByChatId(chatId);
    }

    public void deleteChatMessages(String chatId) {
        List<ChatMessage> chatMessages = chatMessageRepository.findAll();
        for (ChatMessage chatMessage : chatMessages) {
            if (chatMessage.getChatId().equals(chatId)) {
                chatMessageRepository.delete(chatMessage);
            }
        }
    }

    public String getChatRoomId(String senderId, String recipientId, boolean createNewRoomIfNotExists) {
        var first = senderId.compareTo(recipientId) < 0 ? senderId : recipientId;
        var second = senderId.compareTo(recipientId) < 0 ? recipientId : senderId;
        return String.format("%s_%s", first, second);
    }

    public ChatMessageDTO findLastMessage(String chatId) {
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatId(chatId);
        if (chatMessages.size() > 0) {
            ChatMessage c = chatMessages.get(chatMessages.size() - 1);
            ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
            chatMessageDTO.setContent(c.getContent());
            chatMessageDTO.setTimestamp(c.getTimestamp());
            chatMessageDTO.setSenderId(c.getSender().getId());
            chatMessageDTO.setRecipientId(c.getRecipient().getId());
            return chatMessageDTO;
        }
        return null;
    }

    public List<GroupMessageDTO> findGroupMessages(Long groupId) {
        // Get the connected user
        User connectedUser = functions.getConnectedUser();

        // Check if the user is a member of the group before proceeding
        Membership membership = membershipRepository.findByUserIdAndGroupId(connectedUser.getId(), groupId);
        if (membership == null) {
            throw new UnauthorizedAccessException("User does not have access to this group.");
        }

        // Now check for group messages
        List<GroupMessage> groupMessages = groupMessageRepository.findByGroupId(groupId);
        if (groupMessages.isEmpty()) {
            throw new GroupMessageException("No group messages found for group ID: " + groupId);
        }

        // Map the group messages to DTOs
        List<GroupMessageDTO> groupMessages2 = new ArrayList<>();
        for (GroupMessage groupMessage : groupMessages) {
            GroupMessageDTO groupMessageDTO = new GroupMessageDTO();
            groupMessageDTO.setContent(groupMessage.getContent());
            groupMessageDTO.setTimestamp(groupMessage.getTimestamp());
            groupMessageDTO.setSenderId(groupMessage.getSender().getId());
            groupMessageDTO.setSenderName(groupMessage.getSender().getUsername());
            groupMessageDTO.setGroupId(groupMessage.getGroupId());
            groupMessages2.add(groupMessageDTO);
        }

        return groupMessages2;
    }


    public GroupMessageDTO findLastGroupMessage(Long groupId) {
        List<GroupMessage> groupMessages = groupMessageRepository.findByGroupId(groupId);
        if (groupMessages.isEmpty()) {
            throw new GroupNotFoundException(groupId);
        }
        GroupMessage groupMessage2 = groupMessages.get(groupMessages.size() - 1);
        GroupMessageDTO groupMessageDTO = new GroupMessageDTO();
        groupMessageDTO.setContent(groupMessage2.getContent());
        groupMessageDTO.setTimestamp(groupMessage2.getTimestamp());
        groupMessageDTO.setSenderId(groupMessage2.getSender().getId());
        groupMessageDTO.setSenderName(groupMessage2.getSender().getUsername());
        return groupMessageDTO;
    }

    public TypingDTO chatTyping(TypingDTO body) {
        String chatId = getChatRoomId(Long.toString(body.getSenderId()), Long.toString(body.getReceiverId()), true);
        messagingTemplate.convertAndSendToUser(
                chatId, "/queue/typing",
                new TypingDTO(body.getSenderId(), body.getReceiverId())
        );
        return new TypingDTO(body.getSenderId(), body.getReceiverId());
    }

    public GroupTypingDTO groupTyping(GroupTypingDTO body) {
        messagingTemplate.convertAndSendToUser(
                Long.toString(body.getGroupId()), "/queue/typing",
                new GroupTypingDTO(body.getSenderId(), body.getGroupId())
        );
        return new GroupTypingDTO(body.getSenderId(), body.getGroupId());
    }
}