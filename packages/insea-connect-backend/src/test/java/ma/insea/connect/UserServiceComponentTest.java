package ma.insea.connect;

import ma.insea.connect.chat.common.chatMessage.repository.GroupMessageRepository;
import ma.insea.connect.chat.group.repository.MembershipRepository;
import ma.insea.connect.user.DTO.User;
import ma.insea.connect.user.DTO.UserDTO;
import ma.insea.connect.user.model.Status;
import ma.insea.connect.user.repository.UserRepository;
import ma.insea.connect.user.service.UserService;
import ma.insea.connect.utils.Functions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class UserServiceComponentTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MembershipRepository membershipRepository;

    @MockBean
    private GroupMessageRepository groupMessageRepository;

    @MockBean
    private Functions functions;

   /* @Test
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
    }*/
}
