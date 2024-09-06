import ma.insea.connect.chat.common.chatMessage.DTO.ChatMessageDTO;
import ma.insea.connect.chat.common.chatMessage.DTO.GroupMessageDTO;
import ma.insea.connect.chat.common.chatMessage.repository.ChatMessageRepository;
import ma.insea.connect.chat.common.chatMessage.repository.GroupMessageRepository;
import ma.insea.connect.chat.common.chatMessage.service.ChatMessageService;
import ma.insea.connect.chat.group.repository.MembershipRepository;
import ma.insea.connect.exception.ChatException.GroupNotFoundException;
import ma.insea.connect.exception.ChatException.UnauthorizedAccessException;
import ma.insea.connect.exception.ChatException.UserNotFoundException;
import ma.insea.connect.user.DTO.User;
import ma.insea.connect.user.repository.UserRepository;
import ma.insea.connect.utils.Functions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupMessageRepository groupMessageRepository;

    @Mock
    private Functions functions;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private User sender;
    private User recipient;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);

        recipient = new User();
        recipient.setId(2L);
    }

    @Test
    void saveusermessage_UserNotFound_ShouldThrowUserNotFoundException() {
        // Arrange
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setSenderId(1L);
        chatMessageDTO.setRecipientId(2L);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> chatMessageService.saveusermessage(chatMessageDTO));
    }

    @Test
    void savegroupmessage_GroupNotFound_ShouldThrowGroupNotFoundException() {
        // Arrange
        GroupMessageDTO groupMessageDTO = new GroupMessageDTO();
        groupMessageDTO.setSenderId(1L);
        groupMessageDTO.setGroupId(1L);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(sender));
        when(groupMessageRepository.findByGroupId(1L)).thenReturn(java.util.Collections.emptyList());

        // Act & Assert
        assertThrows(GroupNotFoundException.class, () -> chatMessageService.findGroupMessages(1L));
    }

    @Test
    void findGroupMessages_UnauthorizedAccess_ShouldThrowUnauthorizedAccessException() {
        // Arrange
        Long groupId = 1L;
        when(groupMessageRepository.findByGroupId(groupId)).thenReturn(mock(List.class));
        when(functions.getConnectedUser()).thenReturn(sender);
        when(membershipRepository.findByUserIdAndGroupId(sender.getId(), groupId)).thenReturn(null);

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> chatMessageService.findGroupMessages(groupId));
    }
}
