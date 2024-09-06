package ma.insea.connect;

import ma.insea.connect.chat.common.chatMessage.DTO.GroupMessageDTO;
import ma.insea.connect.chat.common.chatMessage.service.ChatMessageService;
import ma.insea.connect.chat.group.DTO.GroupDTO;
import ma.insea.connect.chat.group.DTO.GroupDTO2;
import ma.insea.connect.chat.group.model.Group;
import ma.insea.connect.chat.group.model.Membership;
import ma.insea.connect.chat.group.repository.GroupRepository;
import ma.insea.connect.chat.group.repository.MembershipRepository;
import ma.insea.connect.chat.group.service.GroupService;
import ma.insea.connect.exception.UnauthorizedException;
import ma.insea.connect.user.DTO.User;
import ma.insea.connect.user.DTO.UserDTO3;
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

class GroupServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private Functions functions;

    @InjectMocks
    private GroupService groupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveGroup_validData_savesGroup() {
        // Arrange
        User connectedUser = new User();
        connectedUser.setId(1L);
        connectedUser.setUsername("user1");

        when(functions.getConnectedUser()).thenReturn(connectedUser);

        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setName("Test Group");
        groupDTO.setDescription("Test Description");
        groupDTO.setMembers(new ArrayList<>());

        Group group = new Group();
        group.setId(1L);
        group.setName("Test Group");
        group.setCreator(connectedUser);

        when(groupRepository.save(any(Group.class))).thenReturn(group);
        when(userRepository.findById(1L)).thenReturn(Optional.of(connectedUser));
        when(membershipRepository.findById(any())).thenReturn(Optional.of(new Membership()));

        // Act
        GroupDTO result = groupService.saveGroup(groupDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Test Group", result.getName());
        verify(groupRepository, times(2)).save(any(Group.class)); // Once during creation, once after adding members
    }

    @Test
    void findallgroupsofemail_validUser_returnsGroups() {
        // Arrange
        User connectedUser = new User();
        connectedUser.setId(1L);

        Membership membership = new Membership();
        Group group = new Group();
        group.setId(1L);
        membership.setGroup(group);

        List<Membership> memberships = new ArrayList<>();
        memberships.add(membership);

        when(functions.getConnectedUser()).thenReturn(connectedUser);
        when(membershipRepository.findByUserId(1L)).thenReturn(memberships);

        GroupMessageDTO groupMessageDTO = new GroupMessageDTO();
        groupMessageDTO.setContent("Last Message");
        when(chatMessageService.findLastGroupMessage(anyLong())).thenReturn(groupMessageDTO);

        // Act
        List<GroupDTO2> result = groupService.findallgroupsofemail();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void deleteGroup_validCreator_deletesGroup() {
        // Arrange
        User connectedUser = new User();
        connectedUser.setId(1L);

        Group group = new Group();
        group.setId(1L);
        group.setCreator(connectedUser);

        when(functions.getConnectedUser()).thenReturn(connectedUser);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        // Act
        String result = groupService.deleteGroup(1L);

        // Assert
        assertEquals("Group deleted successfully", result);
        verify(groupRepository, times(1)).delete(group);
    }

    @Test
    void deleteGroup_notCreator_throwsUnauthorizedException() {
        // Arrange
        User connectedUser = new User();
        connectedUser.setId(2L);

        User creator = new User();
        creator.setId(1L);

        Group group = new Group();
        group.setId(1L);
        group.setCreator(creator);

        when(functions.getConnectedUser()).thenReturn(connectedUser);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> groupService.deleteGroup(1L));
    }

    @Test
    void findUsers_validGroupId_returnsUsers() {
        // Arrange
        User connectedUser = new User();
        connectedUser.setId(1L);

        User memberUser = new User();
        memberUser.setId(2L);
        memberUser.setUsername("user2");
        memberUser.setEmail("user2@test.com");

        Group group = new Group();
        group.setId(1L);
        group.setCreator(connectedUser);

        Membership membership = new Membership();
        membership.setGroup(group);
        membership.setUser(memberUser); // Set a valid user in the membership
        membership.setIsAdmin(false);

        List<Membership> memberships = new ArrayList<>();
        memberships.add(membership);

        when(functions.getConnectedUser()).thenReturn(connectedUser);
        when(membershipRepository.findAllByGroupId(1L)).thenReturn(memberships);
        when(membershipRepository.findByUserIdAndGroupId(1L, 1L)).thenReturn(membership);

        // Act
        List<UserDTO3> result = groupService.findUsers(1L);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2L, result.get(0).getId());  // Ensure the correct user is returned
        verify(membershipRepository, times(1)).findAllByGroupId(1L);
    }


    @Test
    void addGroupMembers_notAdmin_throwsUnauthorizedException() {
        // Arrange
        User connectedUser = new User();
        connectedUser.setId(1L);

        Membership membership = new Membership();
        membership.setIsAdmin(false);

        when(functions.getConnectedUser()).thenReturn(connectedUser);
        when(membershipRepository.findByUserIdAndGroupId(1L, 1L)).thenReturn(membership);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> groupService.addGroupMembers(1L, new ArrayList<>()));
    }

    @Test
    void removeGroupMember_notAdmin_throwsUnauthorizedException() {
        // Arrange
        User connectedUser = new User();
        connectedUser.setId(1L);

        Membership membership = new Membership();
        membership.setIsAdmin(false);

        when(functions.getConnectedUser()).thenReturn(connectedUser);
        when(membershipRepository.findByUserIdAndGroupId(1L, 1L)).thenReturn(membership);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> groupService.removeGroupMember(1L, 2L));
    }
}