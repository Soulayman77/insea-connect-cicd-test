package ma.insea.connect.exception.ChatException;

public class ConversationNotFoundException extends RuntimeException {
    public ConversationNotFoundException(String conversationId) {
        super("Conversation with ID " + conversationId + " not found.");
    }
}