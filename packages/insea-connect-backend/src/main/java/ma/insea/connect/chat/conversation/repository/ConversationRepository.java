package ma.insea.connect.chat.conversation.repository;

import java.util.List;

import ma.insea.connect.chat.conversation.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ma.insea.connect.user.DTO.User;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    List<Conversation> findAllByMember1OrMember2(User email, User email2);

    Conversation findByChatId(String conversationId);

}