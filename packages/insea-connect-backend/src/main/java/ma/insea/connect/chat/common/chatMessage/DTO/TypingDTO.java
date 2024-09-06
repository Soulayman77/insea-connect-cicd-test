package ma.insea.connect.chat.common.chatMessage.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypingDTO {
    private Long senderId;
    private Long receiverId;

}
