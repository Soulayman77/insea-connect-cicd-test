package ma.insea.connect;

import ma.insea.connect.chat.common.chatMessage.DTO.ChatMessageDTO;
import ma.insea.connect.chat.common.chatMessage.DTO.ChatMessageDTO2;
import ma.insea.connect.chat.common.chatMessage.model.ChatMessage;
import ma.insea.connect.chat.common.chatMessage.repository.ChatMessageRepository;
import ma.insea.connect.chat.common.chatMessage.service.ChatMessageService;
import ma.insea.connect.chat.conversation.DTO.ConversationDTO;
import ma.insea.connect.chat.conversation.model.Conversation;
import ma.insea.connect.chat.conversation.repository.ConversationRepository;
import ma.insea.connect.chat.conversation.service.ConversationService;
import ma.insea.connect.exception.ChatException.ConversationNotFoundException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private Functions functions;

    @InjectMocks
    private ConversationService conversationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findConversationsByEmail_validUser_returnsConversations() {
        // Arrange
        User connectedUser = new User();
        connectedUser.setEmail("member1@test.com");  // Set connected user email as member1

        User foundUser = new User();
        foundUser.setEmail("member1@test.com");

        // Mock the connected user and the repository to return valid users
        when(functions.getConnectedUser()).thenReturn(connectedUser);
        when(userRepository.findByEmail("member1@test.com")).thenReturn(foundUser);  // Connected user is member1

        // Create a conversation with valid members
        Conversation conversation = new Conversation();
        conversation.setChatId("chat_1");

        // Set member1 and member2 for the conversation
        User member1 = new User();
        member1.setEmail("member1@test.com");
        member1.setUsername("member1");
        conversation.setMember1(member1);  // Ensure member1 is set

        User member2 = new User();
        member2.setEmail("member2@test.com");
        member2.setUsername("member2");
        conversation.setMember2(member2);  // Ensure member2 is set

        List<Conversation> conversations = new ArrayList<>();
        conversations.add(conversation);

        // Mock the conversation repository to return conversations
        when(conversationRepository.findAllByMember1OrMember2(any(User.class), any(User.class))).thenReturn(conversations);

        // Mock userRepository.findByEmail for member1 and member2
        when(userRepository.findByEmail("member2@test.com")).thenReturn(member2);

        // Mock chat message service to return the last message
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setContent("Hello");
        when(chatMessageService.findLastMessage(anyString())).thenReturn(chatMessageDTO);

        // Act
        List<ConversationDTO> result = conversationService.findConversationsByEmail();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("chat_1", result.get(0).getChatId());
        verify(userRepository, times(1)).findByEmail("member1@test.com");
        verify(userRepository, times(1)).findByEmail("member2@test.com");
        verify(conversationRepository, times(1)).findAllByMember1OrMember2(any(User.class), any(User.class));
    }



    @Test
    void findConversationsByEmail_userNotFound_throwsUserNotFoundException() {
        // Arrange
        User connectedUser = new User();
        connectedUser.setEmail("test@test.com");

        when(functions.getConnectedUser()).thenReturn(connectedUser);
        when(userRepository.findByEmail("test@test.com")).thenReturn(null);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> conversationService.findConversationsByEmail());
    }

    @Test
    void findConversationsByID_validUser_returnsConversations() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        // Mock the repository to return valid users for both member1 and member2
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        // Create a conversation with valid members
        Conversation conversation = new Conversation();
        conversation.setChatId("chat_1");
        conversation.setMember1(user1);  // Set member1
        conversation.setMember2(user2);  // Set member2

        List<Conversation> conversations = new ArrayList<>();
        conversations.add(conversation);

        when(conversationRepository.findAllByMember1OrMember2(any(User.class), any(User.class))).thenReturn(conversations);

        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setContent("Hello");
        when(chatMessageService.findLastMessage(anyString())).thenReturn(chatMessageDTO);

        // Act
        List<ConversationDTO> result = conversationService.findConversationsByID(1L);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("chat_1", result.get(0).getChatId());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(2L);  // Ensure member2 is also fetched
    }


    @Test
    void findConversationsByID_userNotFound_throwsUserNotFoundException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> conversationService.findConversationsByID(1L));
    }

    @Test
    void findConversationMessages_validConversationId_returnsMessages() {
        // Arrange
        String conversationId = "chat_1";
        User connectedUser = new User();
        connectedUser.setId(1L);

        Conversation conversation = new Conversation();
        conversation.setMember1(connectedUser);
        conversation.setMember2(new User());

        when(functions.getConnectedUser()).thenReturn(connectedUser);
        when(conversationRepository.findByChatId(conversationId)).thenReturn(conversation);

        // Create a valid ChatMessage object and set the sender
        User sender = new User();
        sender.setId(1L); // Set sender ID
        sender.setUsername("testUser"); // Set sender username

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent("Hello");
        chatMessage.setSender(sender);  // Set the sender to avoid NullPointerException

        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(chatMessage);

        when(chatMessageRepository.findByChatId(conversationId)).thenReturn(chatMessages);

        // Act
        List<ChatMessageDTO2> result = conversationService.findConversationMessages(conversationId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("Hello", result.get(0).getContent());
        assertEquals("testUser", result.get(0).getSenderName());
    }

    @Test
    void findConversationMessages_conversationNotFound_throwsConversationNotFoundException() {
        // Arrange
        String conversationId = "chat_1";
        when(conversationRepository.findByChatId(conversationId)).thenReturn(null);

        // Act & Assert
        assertThrows(ConversationNotFoundException.class, () -> conversationService.findConversationMessages(conversationId));
    }

    @Test
    void findConversationMessages_userNotAuthorized_throwsUnauthorizedAccessException() {
        // Arrange
        String conversationId = "chat_1";
        User connectedUser = new User();
        connectedUser.setId(1L);

        Conversation conversation = new Conversation();
        conversation.setMember1(new User());
        conversation.setMember2(new User());

        when(functions.getConnectedUser()).thenReturn(connectedUser);
        when(conversationRepository.findByChatId(conversationId)).thenReturn(conversation);

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> conversationService.findConversationMessages(conversationId));
    }

    @Test
    void createConversation_validUser_createsConversation() {
        // Arrange
        User connectedUser = new User();
        connectedUser.setId(1L);
        connectedUser.setEmail("test@test.com");

        User recipientUser = new User();
        recipientUser.setId(2L);

        when(functions.getConnectedUser()).thenReturn(connectedUser);
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipientUser));

        when(chatMessageService.getChatRoomId(anyString(), anyString(), eq(true))).thenReturn("chat_1");

        Conversation conversation = new Conversation();
        conversation.setChatId("chat_1");

        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        // Act
        Conversation result = conversationService.createConversation(2L);

        // Assert
        assertNotNull(result);
        assertEquals("chat_1", result.getChatId());
        verify(conversationRepository, times(1)).save(any(Conversation.class));
    }

    @Test
    void createConversation_userNotFound_throwsUserNotFoundException() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> conversationService.createConversation(2L));
    }
}