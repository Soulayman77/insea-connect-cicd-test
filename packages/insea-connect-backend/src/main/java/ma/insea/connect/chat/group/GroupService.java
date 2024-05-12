package ma.insea.connect.chat.group;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ma.insea.connect.user.User;
import ma.insea.connect.user.UserRepository;


@Service
@RequiredArgsConstructor
public class GroupService {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;

    public Group saveGroup(GroupDTO groupDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User connectedUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        
        Group group = new Group();
        
        group.setName(groupDTO.getName());
        group.setCreator(connectedUser);
        group.setIsOfficial(false);
        group.setDescription(groupDTO.getDescription());
        group.setCreatedDate(new java.sql.Date(System.currentTimeMillis()));
        groupRepository.save(group);
        group.setMemberships(new ArrayList<Membership>());
        for (Long user : groupDTO.getMembers()) {
            Membership m = new Membership();
            m.setId(new MembershipKey(user, group.getId()));
            m.setGroup(group);
            m.setUser(userRepository.findById(user).get());
            m.setIsAdmin(false);
            m.setJoiningDate(new java.sql.Date(System.currentTimeMillis()));
            group.addMembership(m);
        }
        groupRepository.save(group);
        Membership m=membershipRepository.findById(new MembershipKey(group.getCreator().getId(), group.getId())).get();
        m.setIsAdmin(true);
        membershipRepository.save(m);
        return group;
    }
    public List<Group> findallgroupsofemail(Long myId) {
        List<Membership> memberships = membershipRepository.findByUserId(myId);
        List<Group> groups = new ArrayList<Group>();
        for (Membership membership : memberships) {
            groups.add(membership.getGroup());
        }
        return groups;
    }
    public void deleteGroup(Long groupId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);

        Group group = groupRepository.findById(groupId).get();
        if (user == group.getCreator()){
            groupRepository.delete(group);    
        }
    }
    public Group findById(Long groupId) {
        Group group=groupRepository.findById(groupId).get();
        return group;
    }
    public List<User> findUsers(Long groupId) {
        List<Membership> membership = membershipRepository.findAllByGroupId(groupId);
        List<User> users = new ArrayList<User>();
        for (Membership m : membership) {
            users.add(m.getUser());
        }

        
        return users;

    }
    public String addGroupMembers(Long groupId, List<Long> users) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User connectedUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        Membership membership = membershipRepository.findByUserIdAndGroupId(connectedUser.getId(), groupId);
        
        if(membership == null || !membership.getIsAdmin()) {
            return "You are not allowed to add members to this group";
        }else{
        for (Long user : users) {
            Membership m = new Membership();
            m.setId(new MembershipKey(user, groupId));
            m.setGroup(groupRepository.findById(groupId).get());
            m.setUser(userRepository.findById(user).get());
            m.setIsAdmin(false);
            m.setJoiningDate(new java.sql.Date(System.currentTimeMillis()));
            membershipRepository.save(m);
        }
        return "Group members added successfully";
    }
        
    }
    @Transactional
    public String removeGroupMember(Long groupId, Long memberId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User connectedUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        Membership membership = membershipRepository.findByUserIdAndGroupId(connectedUser.getId(), groupId);
        
        if(membership == null || !membership.getIsAdmin()) {
            return "You are not allowed to add members to this group";
        }else{
        membershipRepository.deleteByGroupIdAndUserId(groupId, memberId);
        return "Group member removed successfully";}
    }
    
}
