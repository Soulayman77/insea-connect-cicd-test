package ma.insea.connect;

import ma.insea.connect.chat.common.chatMessage.DTO.ChatMessageDTO;
import ma.insea.connect.chat.common.chatMessage.DTO.GroupMessageDTO;
import ma.insea.connect.chat.common.chatMessage.model.ChatMessage;
import ma.insea.connect.chat.common.chatMessage.model.GroupMessage;
import ma.insea.connect.chat.common.chatMessage.repository.ChatMessageRepository;
import ma.insea.connect.chat.common.chatMessage.repository.GroupMessageRepository;
import ma.insea.connect.chat.common.chatMessage.service.ChatMessageService;
import ma.insea.connect.chat.group.model.Group;
import ma.insea.connect.chat.group.model.Membership;
import ma.insea.connect.chat.group.repository.MembershipRepository;
import ma.insea.connect.exception.ChatException.GroupMessageException;
import ma.insea.connect.exception.userExceptions.UnauthorizedAccessException;
import ma.insea.connect.exception.userExceptions.UserNotFoundException;
import ma.insea.connect.user.DTO.User;
import ma.insea.connect.user.repository.UserRepository;
import ma.insea.connect.utils.Functions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupMessageRepository groupMessageRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private Functions functions;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatMessageService chatMessageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveUserMessage_validData_returnsSavedMessage() {
        // Arrange
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setSenderId(1L);
        chatMessageDTO.setRecipientId(2L);
        chatMessageDTO.setContent("Hello");

        User sender = new User();
        User recipient = new User();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(new ChatMessage());

        // Act
        ChatMessage result = chatMessageService.saveusermessage(chatMessageDTO);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(2L);
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    void saveUserMessage_userNotFound_throwsUserNotFoundException() {
        // Arrange
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setSenderId(1L);
        chatMessageDTO.setRecipientId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> chatMessageService.saveusermessage(chatMessageDTO));
    }

    @Test
    void saveGroupMessage_validData_returnsSavedGroupMessage() {
        // Arrange
        GroupMessageDTO groupMessageDTO = new GroupMessageDTO();
        groupMessageDTO.setSenderId(1L);
        groupMessageDTO.setGroupId(2L);
        groupMessageDTO.setContent("Hello group");

        User sender = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(groupMessageRepository.save(any(GroupMessage.class))).thenReturn(new GroupMessage());

        // Act
        GroupMessage result = chatMessageService.savegroupmessage(groupMessageDTO);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(groupMessageRepository, times(1)).save(any(GroupMessage.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    void saveGroupMessage_userNotFound_throwsUserNotFoundException() {
        // Arrange
        GroupMessageDTO groupMessageDTO = new GroupMessageDTO();
        groupMessageDTO.setSenderId(1L);
        groupMessageDTO.setGroupId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> chatMessageService.savegroupmessage(groupMessageDTO));
    }

    @Test
    void findLastMessage_validChatId_returnsChatMessageDTO() {
        // Arrange
        String chatId = "1_2";
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent("Hello");
        chatMessage.setTimestamp(new Date());
        chatMessage.setSender(new User());
        chatMessage.setRecipient(new User());

        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(chatMessage);

        when(chatMessageRepository.findByChatId(chatId)).thenReturn(chatMessages);

        // Act
        ChatMessageDTO result = chatMessageService.findLastMessage(chatId);

        // Assert
        assertNotNull(result);
        assertEquals("Hello", result.getContent());
    }

    @Test
    void findLastMessage_noMessages_returnsNull() {
        // Arrange
        String chatId = "1_2";
        when(chatMessageRepository.findByChatId(chatId)).thenReturn(new ArrayList<>());

        // Act
        ChatMessageDTO result = chatMessageService.findLastMessage(chatId);

        // Assert
        assertNull(result);
    }

    @Test
    void findGroupMessages_validGroupId_returnsMessages() {
        // Arrange
        Long groupId = 1L;
        List<GroupMessage> groupMessages = new ArrayList<>();
        GroupMessage message = new GroupMessage();
        message.setContent("Hello");

        // Set a valid sender for the group message
        User sender = new User();
        sender.setId(1L); // Set the ID for the sender
        sender.setUsername("testUser");
        message.setSender(sender);  // Ensure the sender is set

        groupMessages.add(message);

        Membership membership = new Membership();
        User connectedUser = new User();
        connectedUser.setId(1L);

        when(functions.getConnectedUser()).thenReturn(connectedUser);
        when(groupMessageRepository.findByGroupId(groupId)).thenReturn(groupMessages);
        when(membershipRepository.findByUserIdAndGroupId(anyLong(), anyLong())).thenReturn(membership);

        // Act
        List<GroupMessageDTO> result = chatMessageService.findGroupMessages(groupId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(groupMessageRepository, times(1)).findByGroupId(groupId);
    }


    @Test
    void findGroupMessages_noMessages_throwsGroupMessageException() {
        // Arrange
        Long groupId = 1L;
        User connectedUser = new User();
        connectedUser.setId(1L);

        // Mock the connected user
        when(functions.getConnectedUser()).thenReturn(connectedUser);

        // Mock the group and membership
        Group group = new Group();
        group.setId(groupId);

        Membership membership = new Membership();
        membership.setGroup(group);  // Set the Group object, not groupId
        membership.setUser(connectedUser);  // Set the User object

        // Mock membership repository to return the membership
        when(membershipRepository.findByUserIdAndGroupId(connectedUser.getId(), groupId)).thenReturn(membership);

        // Mock the groupMessageRepository to return an empty list
        when(groupMessageRepository.findByGroupId(groupId)).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(GroupMessageException.class, () -> chatMessageService.findGroupMessages(groupId));

        // Verify that the repository was called
        verify(groupMessageRepository, times(1)).findByGroupId(groupId);
        verify(membershipRepository, times(1)).findByUserIdAndGroupId(connectedUser.getId(), groupId);
    }


    @Test
    void findGroupMessages_userNotInGroup_throwsUnauthorizedAccessException() {
        // Arrange
        Long groupId = 1L;
        User connectedUser = new User();
        connectedUser.setId(1L);

        when(functions.getConnectedUser()).thenReturn(connectedUser);
        when(membershipRepository.findByUserIdAndGroupId(anyLong(), anyLong())).thenReturn(null);

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> chatMessageService.findGroupMessages(groupId));

        // Verify that no messages were fetched
        verify(groupMessageRepository, never()).findByGroupId(anyLong());
    }
}