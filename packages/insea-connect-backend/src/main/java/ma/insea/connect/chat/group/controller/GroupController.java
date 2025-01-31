package ma.insea.connect.chat.group.controller;

import lombok.AllArgsConstructor;
import ma.insea.connect.chat.common.chatMessage.service.ChatMessageService;
import ma.insea.connect.chat.common.chatMessage.DTO.GroupMessageDTO;
import ma.insea.connect.chat.group.DTO.GroupDTO;
import ma.insea.connect.chat.group.DTO.GroupDTO3;
import ma.insea.connect.chat.group.service.GroupService;
import ma.insea.connect.user.DTO.UserDTO3;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class GroupController {

    private final GroupService groupService;
    private final ChatMessageService chatMessageService;

    @PostMapping("/groups")
    public GroupDTO addGroup(@RequestBody GroupDTO groupDTO) {
        return groupService.saveGroup(groupDTO);
    }
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<String> deleteGroup(@PathVariable("groupId") Long groupId) {
            return ResponseEntity.ok(groupService.deleteGroup(groupId));//cases for response ent
    }

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<GroupDTO3> getGroupInfo(@PathVariable("groupId") Long groupId) {
            
            return ResponseEntity.ok(groupService.getGroup(groupId));//cases for response ent
    }

    @GetMapping("/groups/{groupId}/messages")
    public ResponseEntity<List<GroupMessageDTO>> findGroupChatMessages(@PathVariable Long groupId) {
        
        return ResponseEntity
                .ok(chatMessageService.findGroupMessages(groupId));
    }

    @GetMapping("/groups/{groupId}/members")
    public ResponseEntity<List<UserDTO3>> findGroupUsers(@PathVariable("groupId") Long groupId) {
        return ResponseEntity.ok(groupService.findUsers(groupId));
    }
    
    @PostMapping("/groups/{groupId}/members")
    public ResponseEntity<String> addGroupMembers(@PathVariable("groupId") Long groupId, @RequestBody Map<String, List<Long>>users) {
        Boolean added=groupService.addGroupMembers(groupId, users.get("members"));
        if(!added){
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);}
        else{
            return new ResponseEntity(HttpStatus.OK);}

        
    }

    @DeleteMapping("/groups/{groupId}/members/{memberid}")
    public ResponseEntity<String> removeGroupMember(@PathVariable("groupId") Long groupId, @PathVariable("memberid") Long memberId) {
        Boolean deleted=groupService.removeGroupMember(groupId, memberId);
        if(!deleted){
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);}
        else{
            return new ResponseEntity(HttpStatus.OK);}
    }
    @PostMapping("/groups/{groupId}/admins")
    public ResponseEntity<?> addAdmin(@RequestBody Map<String, Long> userId, @PathVariable("groupId") Long groupId) {
        groupService.addAdmin(groupId, userId.get("userId"));
        return ResponseEntity.ok(null);    
    }
    @DeleteMapping("/groups/{groupId}/admins/{userId}")
    public ResponseEntity<?> removeAdmin(@PathVariable("groupId") Long groupId, @PathVariable("userId") Long userId) {
        groupService.removeAdmin(groupId, userId);
        return ResponseEntity.ok(null);    
    }
    
}
