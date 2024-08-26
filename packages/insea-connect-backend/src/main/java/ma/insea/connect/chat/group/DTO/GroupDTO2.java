package ma.insea.connect.chat.group;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.insea.connect.chat.common.chatMessage.DTO.GroupMessageDTO;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupDTO2 {
    private Long id;
    private String name;
    private GroupMessageDTO lastMessage;
}
