package ma.insea.connect;

import ma.insea.connect.chat.common.chatMessage.model.GroupMessage;
import ma.insea.connect.chat.common.chatMessage.repository.GroupMessageRepository;
import ma.insea.connect.chat.common.chatMessage.service.ChatMessageService;
import ma.insea.connect.chat.group.repository.MembershipRepository;
import ma.insea.connect.exception.userExceptions.InvalidUserDataException;
import ma.insea.connect.exception.userExceptions.UnauthorizedAccessException;
import ma.insea.connect.exception.userExceptions.UserNotFoundException;
import ma.insea.connect.user.DTO.OnlineDTO;
import ma.insea.connect.user.DTO.User;
import ma.insea.connect.user.DTO.UserDTO;
import ma.insea.connect.user.model.Status;
import ma.insea.connect.user.repository.UserRepository;
import ma.insea.connect.user.service.UserService;
import ma.insea.connect.utils.Functions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private GroupMessageRepository groupMessageRepository;

    @Mock
    private Functions functions;

    @InjectMocks
    private UserService userService;

    @InjectMocks
    private ChatMessageService chatMessageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveUser_validUser_returnsSavedUser() {
        // Arrange
        User user = new User();
        user.setEmail("test@test.com");
        user.setUsername("testuser");

        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User savedUser = userService.saveUser(user);

        // Assert
        assertNotNull(savedUser);
        assertEquals("test@test.com", savedUser.getEmail());
        assertEquals(Status.ONLINE, savedUser.getStatus());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void saveUser_invalidUser_throwsInvalidUserDataException() {
        // Arrange
        User invalidUser = new User();

        // Act & Assert
        assertThrows(InvalidUserDataException.class, () -> userService.saveUser(invalidUser));
    }

    @Test
    void disconnect_validUser_updatesStatus() {
        // Arrange
        User user = new User();
        user.setEmail("test@test.com");

        User storedUser = new User();
        storedUser.setEmail("test@test.com");

        when(userRepository.findByEmail(anyString())).thenReturn(storedUser);

        // Act
        userService.disconnect(user);

        // Assert
        verify(userRepository, times(1)).save(storedUser);
        assertEquals(Status.OFFLINE, storedUser.getStatus());
        assertNotNull(storedUser.getLastLogin());
    }

    @Test
    void findGroupMessages_userNotInGroup_throwsUnauthorizedAccessException() {
        // Arrange
        Long groupId = 1L;
        User connectedUser = new User();
        connectedUser.setId(1L);

        when(functions.getConnectedUser()).thenReturn(connectedUser);
        when(membershipRepository.findByUserIdAndGroupId(anyLong(), anyLong())).thenReturn(null);  // User is not in group

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> chatMessageService.findGroupMessages(groupId));

        // Verify that no messages were fetched
        verify(groupMessageRepository, never()).findByGroupId(anyLong());
    }

    @Test
    void findAllUsers_returnsListOfUsers() {
        // Arrange
        List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@test.com");
        users.add(user1);

        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserDTO> userDTOs = userService.findAllUsers();

        // Assert
        assertFalse(userDTOs.isEmpty());
        assertEquals(1, userDTOs.size());
        assertEquals("user1", userDTOs.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void findAllUsers_noUsersFound_throwsUserNotFoundException() {
        // Arrange
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.findAllUsers());
    }

    @Test
    void getUserStatus_userFound_returnsOnlineDTO() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setStatus(Status.ONLINE);
        user.setLastLogin(new java.util.Date());

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // Act
        OnlineDTO onlineDTO = userService.getUserStatus(1L);

        // Assert
        assertNotNull(onlineDTO);
        assertEquals(Status.ONLINE, onlineDTO.getStatus());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserStatus_userNotFound_throwsUserNotFoundException() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUserStatus(1L));
    }

    @Test
    void updateUserLastSeen_validUser_updatesLastLoginAndStatus() {
        // Arrange
        User connectedUser = new User();
        connectedUser.setEmail("test@test.com");

        when(functions.getConnectedUser()).thenReturn(connectedUser);

        // Act
        userService.updateUserLastSeen(Status.ONLINE);

        // Assert
        verify(userRepository, times(1)).save(connectedUser);
        assertEquals(Status.ONLINE, connectedUser.getStatus());
        assertNotNull(connectedUser.getLastLogin());
    }

    @Test
    void checkUserStatuses_updatesUserStatusesToOffline() {
        // Arrange
        List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setLastLogin(java.util.Date.from(LocalDateTime.now().minusMinutes(2).atZone(ZoneId.systemDefault()).toInstant()));
        users.add(user1);

        when(userRepository.findAll()).thenReturn(users);

        // Act
        userService.checkUserStatuses();

        // Assert
        verify(userRepository, times(1)).save(user1);
        assertEquals(Status.OFFLINE, user1.getStatus());
    }

    @Test
    void findByUsername_userFound_returnsUser() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        // Act
        User foundUser = userService.findByUsername("testuser");

        // Assert
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void findByUsername_userNotFound_throwsUserNotFoundException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.findByUsername("testuser"));
    }

    @Test
    void findByEmail_userFound_returnsUser() {
        // Arrange
        User user = new User();
        user.setEmail("test@test.com");

        when(userRepository.findByEmail(anyString())).thenReturn(user);

        // Act
        User foundUser = userService.findByEmail("test@test.com");

        // Assert
        assertNotNull(foundUser);
        assertEquals("test@test.com", foundUser.getEmail());
        verify(userRepository, times(1)).findByEmail("test@test.com");
    }

    @Test
    void findByEmail_userNotFound_throwsUserNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(null);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.findByEmail("test@test.com"));
    }
}