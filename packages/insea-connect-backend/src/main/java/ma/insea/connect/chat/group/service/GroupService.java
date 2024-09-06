package ma.insea.connect.chat.group.service;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ma.insea.connect.chat.group.DTO.GroupDTO;
import ma.insea.connect.chat.group.DTO.GroupDTO2;
import ma.insea.connect.chat.group.DTO.GroupDTO3;
import ma.insea.connect.chat.group.model.Group;
import ma.insea.connect.chat.group.model.Membership;
import ma.insea.connect.chat.group.model.MembershipKey;
import ma.insea.connect.chat.group.repository.GroupRepository;
import ma.insea.connect.chat.group.repository.MembershipRepository;
import ma.insea.connect.exception.ChatException.GroupNotFoundException;
import ma.insea.connect.exception.ChatException.MembershipNotFoundException;
import ma.insea.connect.exception.userExceptions.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ma.insea.connect.chat.common.chatMessage.service.ChatMessageService;
import ma.insea.connect.chat.common.chatMessage.DTO.GroupMessageDTO;
import ma.insea.connect.exception.UnauthorizedException;
import ma.insea.connect.user.DTO.User;
import ma.insea.connect.user.DTO.UserDTO3;
import ma.insea.connect.user.repository.UserRepository;
import ma.insea.connect.utils.Functions;


@Service
@RequiredArgsConstructor
public class GroupService {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final ChatMessageService chatMessageService;
    private final Functions functions;

    public GroupDTO saveGroup(GroupDTO groupDTO) {
        User connectedUser = functions.getConnectedUser();
        Group group = new Group();

        group.setName(groupDTO.getName());
        group.setCreator(connectedUser);
        group.setIsOfficial(false);
        group.setDescription(groupDTO.getDescription());
        group.setCreatedDate(new java.util.Date(System.currentTimeMillis()));
        groupRepository.save(group);

        List<Long> mem = groupDTO.getMembers();
        mem.add(connectedUser.getId());
        group.setMemberships(new ArrayList<Membership>());

        for (Long userId : mem) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));
            Membership m = new Membership();
            m.setId(new MembershipKey(userId, group.getId()));
            m.setGroup(group);
            m.setUser(user);
            m.setIsAdmin(false);
            m.setJoiningDate(new java.util.Date(System.currentTimeMillis()));
            group.addMembership(m);
        }

        groupRepository.save(group);

        Membership m = membershipRepository.findById(new MembershipKey(group.getCreator().getId(), group.getId()))
                .orElseThrow(() -> new MembershipNotFoundException(group.getCreator().getId(), group.getId()));
        m.setIsAdmin(true);
        membershipRepository.save(m);

        groupDTO.setId(group.getId());
        return groupDTO;
    }

    public List<GroupDTO2> findallgroupsofemail() {
        User connectedUser = functions.getConnectedUser();
        List<Membership> memberships = membershipRepository.findByUserId(connectedUser.getId());
        List<Group> groups = new ArrayList<>();

        for (Membership membership : memberships) {
            groups.add(membership.getGroup());
        }

        List<GroupDTO2> groupDTOs = new ArrayList<>();
        for (Group group : groups) {
            GroupDTO2 groupDTO = new GroupDTO2();
            groupDTO.setId(group.getId());
            groupDTO.setName(group.getName());
            GroupMessageDTO chatMessage = chatMessageService.findLastGroupMessage(group.getId());
            groupDTO.setLastMessage(chatMessage);
            groupDTOs.add(groupDTO);
        }

        Collections.reverse(groupDTOs);
        groupDTOs.sort(Comparator.comparing(
                (GroupDTO2 groupDTO2) -> groupDTO2.getLastMessage() != null ? groupDTO2.getLastMessage().getTimestamp() : new Date(0)
        ).reversed());

        return groupDTOs;
    }

    public String deleteGroup(Long groupId) {
        User connectedUser = functions.getConnectedUser();

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        if (connectedUser.equals(group.getCreator())) {
            groupRepository.delete(group);
            return "Group deleted successfully";
        } else {
            throw new UnauthorizedException("You are not allowed to delete this group");
        }
    }

    public Group findById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    public List<UserDTO3> findUsers(Long groupId) {
        List<Membership> membership = membershipRepository.findAllByGroupId(groupId);
        List<UserDTO3> groupMembersDTOs = new ArrayList<>();

        for (Membership m : membership) {
            User user = m.getUser();
            UserDTO3 userDTO = new UserDTO3(user.getId(), user.getUsername(), user.getEmail(), m.getIsAdmin(), user.getId().equals(m.getGroup().getCreator().getId()));
            groupMembersDTOs.add(userDTO);
        }

        User connectedUser = functions.getConnectedUser();
        Membership membership2 = membershipRepository.findByUserIdAndGroupId(connectedUser.getId(), groupId);
        if (membership2 == null) {
            throw new MembershipNotFoundException(connectedUser.getId(), groupId);
        }
        return groupMembersDTOs;
    }

    public Boolean addGroupMembers(Long groupId, List<Long> users) {
        User connectedUser = functions.getConnectedUser();
        Membership membership = membershipRepository.findByUserIdAndGroupId(connectedUser.getId(), groupId);

        if (membership == null || !membership.getIsAdmin()) {
            throw new UnauthorizedException("You are not allowed to add members to this group");
        }

        for (Long userId : users) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));

            Membership m = new Membership();
            m.setId(new MembershipKey(userId, groupId));
            m.setGroup(groupRepository.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException(groupId)));
            m.setUser(user);
            m.setIsAdmin(false);
            m.setJoiningDate(new java.util.Date(System.currentTimeMillis()));
            membershipRepository.save(m);
        }

        return true;
    }

    @Transactional
    public Boolean removeGroupMember(Long groupId, Long memberId) {
        User connectedUser = functions.getConnectedUser();
        Membership membership = membershipRepository.findByUserIdAndGroupId(connectedUser.getId(), groupId);

        if (membership == null || !membership.getIsAdmin()) {
            throw new UnauthorizedException("You are not allowed to remove members from this group");
        }

        membershipRepository.deleteByGroupIdAndUserId(groupId, memberId);
        return true;
    }

    public GroupDTO3 getGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
        User connectedUser = functions.getConnectedUser();
        Membership membership = membershipRepository.findByUserIdAndGroupId(connectedUser.getId(), groupId);

        if (membership == null) {
            throw new MembershipNotFoundException(connectedUser.getId(), groupId);
        }

        return new GroupDTO3(group.getId(), group.getImagrUrl(), group.getName(), group.getDescription(), group.getIsOfficial(), group.getCreatedDate());
    }

    public void addAdmin(Long groupId, Long long1) {
        User connectedUser = functions.getConnectedUser();
        Membership membership = membershipRepository.findByUserIdAndGroupId(connectedUser.getId(), groupId);
        Membership membership2 = membershipRepository.findByUserIdAndGroupId(long1, groupId);

        if (membership2 == null) {
            throw new UnauthorizedException("User is not a member of this group");
        }

        if (membership == null || !membership.getIsAdmin()) {
            throw new UnauthorizedException("You are not allowed to add admins to this group");
        }

        Membership m = membershipRepository.findById(new MembershipKey(long1, groupId))
                .orElseThrow(() -> new MembershipNotFoundException(long1, groupId));
        m.setIsAdmin(true);
        membershipRepository.save(m);
    }

    public void removeAdmin(Long groupId, Long long1) {
        User connectedUser = functions.getConnectedUser();
        Membership membership = membershipRepository.findByUserIdAndGroupId(connectedUser.getId(), groupId);
        Membership membership2 = membershipRepository.findByUserIdAndGroupId(long1, groupId);

        if (membership2 == null) {
            throw new UnauthorizedException("User is not a member of this group");
        }

        if (membership == null || !membership.getIsAdmin()) {
            throw new UnauthorizedException("You are not allowed to remove admins from this group");
        }

        Membership m = membershipRepository.findById(new MembershipKey(long1, groupId))
                .orElseThrow(() -> new MembershipNotFoundException(long1, groupId));
        m.setIsAdmin(false);
        membershipRepository.save(m);
    }
}

